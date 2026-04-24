package com.littlewin.common.utils;

import com.littlewin.common.core.TreeNode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 树形结构通用构建工具类
 * <p>
 * 使用场景：
 * - 菜单树、分类树、组织架构树等（支持最多三层结构）
 * - 性能优化：使用Map索引，时间复杂度 O(n)
 * </p>
 *
 * @author LittleWin
 */
@Slf4j
public class TreeUtils {

    private TreeUtils() {
        // 私有构造器，防止实例化
    }

    /**
     * 构建嵌套树形结构
     *
     * @param list         所有节点数据（扁平列表）
     * @param rootParentId 根节点的父ID (例如 0L 或 null)
     * @param <T>          节点类型
     * @param <K>          ID类型
     * @return 组装好的树形列表
     */
    public static <T extends TreeNode<T, K>, K> List<T> build(List<T> list, K rootParentId) {
        return build(list, rootParentId, null);
    }

    /**
     * 构建嵌套树形结构并排序
     *
     * @param list         所有节点数据（扁平列表）
     * @param rootParentId 根节点的父ID (例如 0L 或 null)
     * @param comparator   比较器（通常按 sortOrder 排序），可为null
     * @param <T>          节点类型
     * @param <K>          ID类型
     * @return 组装好的树形列表
     */
    public static <T extends TreeNode<T, K>, K> List<T> build(List<T> list, K rootParentId, Comparator<T> comparator) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 过滤 null 元素，避免 NPE
        List<T> filteredList = list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建 ID -> 节点映射，使用 LinkedHashMap 保持插入顺序
        Map<K, T> idToNodeMap = filteredList.stream()
                .collect(Collectors.toMap(
                        TreeNode::getId,
                        node -> node,
                        (existing, replacement) -> existing,  // 冲突时保留第一个
                        LinkedHashMap::new
                ));

        List<T> roots = new ArrayList<>();

        // 遍历组装树结构
        for (T node : filteredList) {
            K parentId = node.getParentId();

            if (Objects.equals(parentId, rootParentId)) {
                // 根节点
                roots.add(node);
            } else if (parentId != null) {
                // 非根节点，尝试挂载到父节点
                T parent = idToNodeMap.get(parentId);
                if (parent != null) {
                    // 父节点存在，添加为子节点
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                } else {
                    // 父节点不存在（数据不完整），记录警告（可选）
                    log.debug("Parent node not found for node id: {}, parentId: {}", node.getId(), parentId);
                }
            }
        }

        // 排序
        if (comparator != null) {
            sortTree(roots, comparator);
        }

        return roots;
    }

    /**
     * 获取指定节点下的所有子 ID 集合（不含自身）
     * <p>
     * 使用预处理 Map，避免在递归中全列表扫描
     * 对于最多三层的结构，递归深度最大为2，极其安全
     * </p>
     *
     * @param all      所有节点数据（一次性从数据库查出的全量数据）
     * @param parentId 需要查找的起始父ID
     * @param <T>      节点类型
     * @param <K>      ID类型
     * @return 包含所有子节点 ID 的列表（不含 parentId 本身）
     */
    public static <T extends TreeNode<T, K>, K> List<K> findAllChildIds(List<T> all, K parentId) {
        return findAllChildIds(all, parentId, false);
    }

    /**
     * 获取指定节点下的所有子 ID 集合
     *
     * @param all         所有节点数据
     * @param parentId    起始父ID
     * @param includeSelf 是否包含 parentId 本身
     * @param <T>         节点类型
     * @param <K>         ID类型
     * @return 子节点ID列表
     */
    public static <T extends TreeNode<T, K>, K> List<K> findAllChildIds(List<T> all, K parentId, boolean includeSelf) {
        if (all == null || all.isEmpty() || parentId == null) {
            return includeSelf && parentId != null ? Collections.singletonList(parentId) : Collections.emptyList();
        }

        // 预处理：按 parentId 分组，过滤掉 parentId 为 null 的节点
        Map<K, List<T>> parentToChildrenMap = all.stream()
                .filter(Objects::nonNull)
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(
                        TreeNode::getParentId,
                        Collectors.toList()
                ));

        List<K> result = new ArrayList<>();
        collectChildIds(parentId, parentToChildrenMap, result);

        if (includeSelf) {
            result.add(0, parentId);
        }

        return result;
    }

    /**
     * 获取指定节点下的所有子节点（扁平化列表）
     *
     * @param all      所有节点数据
     * @param parentId 起始父ID
     * @param <T>      节点类型
     * @param <K>      ID类型
     * @return 所有子节点列表（不含自身）
     */
    public static <T extends TreeNode<T, K>, K> List<T> findAllChildNodes(List<T> all, K parentId) {
        if (all == null || all.isEmpty() || parentId == null) {
            return new ArrayList<>();
        }

        // 预处理：按 parentId 分组
        Map<K, List<T>> parentToChildrenMap = all.stream()
                .filter(Objects::nonNull)
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(TreeNode::getParentId));

        List<T> result = new ArrayList<>();
        collectChildNodes(parentId, parentToChildrenMap, result);
        return result;
    }

    /**
     * 私有递归辅助方法：收集子节点ID
     *
     * @param parentId   当前父ID
     * @param parentMap  父ID -> 子节点列表映射
     * @param result     结果收集器
     * @param <T>        节点类型
     * @param <K>        ID类型
     */
    private static <T extends TreeNode<T, K>, K> void collectChildIds(K parentId, Map<K, List<T>> parentMap, List<K> result) {
        List<T> children = parentMap.get(parentId);
        if (children != null) {
            for (T child : children) {
                K childId = child.getId();
                if (childId != null) {
                    result.add(childId);
                    collectChildIds(childId, parentMap, result);
                }
            }
        }
    }

    /**
     * 私有递归辅助方法：收集子节点对象
     *
     * @param parentId   当前父ID
     * @param parentMap  父ID -> 子节点列表映射
     * @param result     结果收集器
     * @param <T>        节点类型
     * @param <K>        ID类型
     */
    private static <T extends TreeNode<T, K>, K> void collectChildNodes(K parentId, Map<K, List<T>> parentMap, List<T> result) {
        List<T> children = parentMap.get(parentId);
        if (children != null) {
            for (T child : children) {
                result.add(child);
                collectChildNodes(child.getId(), parentMap, result);
            }
        }
    }

    /**
     * 递归排序树结构
     *
     * @param nodes      需要排序的节点列表
     * @param comparator 比较器
     * @param <T>        节点类型
     * @param <K>        ID类型
     */
    private static <T extends TreeNode<T, K>, K> void sortTree(List<T> nodes, Comparator<T> comparator) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(comparator);
        for (T node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren(), comparator);
            }
        }
    }

    /**
     * 扁平化树结构（将树转为列表，便于后续处理）
     *
     * @param tree 树形结构列表
     * @param <T>  节点类型
     * @param <K>  ID类型
     * @return 扁平化后的列表（深度优先遍历）
     */
    public static <T extends TreeNode<T, K>, K> List<T> flatten(List<T> tree) {
        List<T> result = new ArrayList<>();
        if (tree == null || tree.isEmpty()) {
            return result;
        }

        Deque<T> stack = new ArrayDeque<>();
        // 逆序入栈，保持原始顺序
        for (int i = tree.size() - 1; i >= 0; i--) {
            stack.push(tree.get(i));
        }

        while (!stack.isEmpty()) {
            T node = stack.pop();
            result.add(node);
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                // 子节点逆序入栈，保持原始顺序
                List<T> children = node.getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
        }
        return result;
    }

    /**
     * 查找从根节点到指定节点的路径
     *
     * @param all    所有节点数据
     * @param nodeId 目标节点ID
     * @param <T>    节点类型
     * @param <K>    ID类型
     * @return 路径列表（从根节点到目标节点），如果未找到返回空列表
     */
    public static <T extends TreeNode<T, K>, K> List<T> findPath(List<T> all, K nodeId) {
        if (all == null || all.isEmpty() || nodeId == null) {
            return new ArrayList<>();
        }

        // 构建 ID -> 节点的映射
        Map<K, T> idToNodeMap = all.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TreeNode::getId, node -> node, (v1, v2) -> v1));

        T target = idToNodeMap.get(nodeId);
        if (target == null) {
            return new ArrayList<>();
        }

        // 向上追溯父节点
        LinkedList<T> path = new LinkedList<>();
        T current = target;
        while (current != null) {
            path.addFirst(current);
            K parentId = current.getParentId();
            if (parentId == null) {
                break;
            }
            current = idToNodeMap.get(parentId);
        }
        return path;
    }

    /**
     * 验证树结构的完整性（检查是否存在孤儿节点）
     *
     * @param all          所有节点数据
     * @param rootParentId 根节点的父ID
     * @param <T>          节点类型
     * @param <K>          ID类型
     * @return 孤儿节点列表（父节点不存在的节点）
     */
    public static <T extends TreeNode<T, K>, K> List<T> findOrphanNodes(List<T> all, K rootParentId) {
        if (all == null || all.isEmpty()) {
            return new ArrayList<>();
        }

        Set<K> allIds = all.stream()
                .filter(Objects::nonNull)
                .map(TreeNode::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return all.stream()
                .filter(Objects::nonNull)
                .filter(node -> {
                    K parentId = node.getParentId();
                    return parentId != null
                            && !Objects.equals(parentId, rootParentId)
                            && !allIds.contains(parentId);
                })
                .collect(Collectors.toList());
    }
}