package com.littlewin.common.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 树节点接口
 * <p>
 * 实现此接口的类可以使用 TreeUtils 进行树形结构操作
 * </p>
 *
 * @param <T> 节点类型（通常是实现类自身）
 * @param <K> ID类型（如 Long, String, Integer）
 * @author LittleWin
 */
public interface TreeNode<T, K> {

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    K getId();

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    K getParentId();

    /**
     * 获取子节点列表
     *
     * @return 子节点列表
     */
    List<T> getChildren();

    /**
     * 设置子节点列表
     *
     * @param children 子节点列表
     */
    void setChildren(List<T> children);

    /**
     * 添加子节点（默认实现）
     *
     * @param child 子节点
     */
    default void addChild(T child) {
        if (getChildren() == null) {
            setChildren(new ArrayList<>());
        }
        getChildren().add(child);
    }

    /**
     * 是否有子节点
     *
     * @return true: 有子节点, false: 无子节点
     */
    default boolean hasChildren() {
        return getChildren() != null && !getChildren().isEmpty();
    }

    /**
     * 获取子节点数量
     *
     * @return 子节点数量
     */
    default int childrenCount() {
        return hasChildren() ? getChildren().size() : 0;
    }

    /**
     * 是否为叶子节点
     *
     * @return true: 叶子节点, false: 非叶子节点
     */
    default boolean isLeaf() {
        return !hasChildren();
    }
}