package com.littlewin.note.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.domain.vo.FavoriteNoteVO;
import com.littlewin.note.domain.vo.LikedNoteVO;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.mapper.NoteReactionMapper;
import com.littlewin.note.mapper.NoteTagRelMapper;
import com.littlewin.note.mapper.SysCategoryMapper;
import com.littlewin.note.service.NoteStatsService;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteStatsServiceImpl implements NoteStatsService {

    private final NoteMapper noteMapper;
    private final NoteReactionMapper noteReactionMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final SysCategoryMapper sysCategoryMapper;

    @Override
    public NoteStatsVO getUserStats(Long userId) {
        // 1. 查询笔记数量
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId)
                .eq(Note::getStatus, 1)
                .eq(Note::getDelFlag, 0);
        Long notesCount = noteMapper.selectCount(noteWrapper);

        // 2. 查询获赞数量
        Long likesCount = noteReactionMapper.countLikesByUserNotes(userId);

        // 3. 查询收藏数量
        LambdaQueryWrapper<NoteReaction> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(NoteReaction::getUserId, userId)
                .eq(NoteReaction::getIsFavorite, 1);
        Long favoritesCount = noteReactionMapper.selectCount(favoriteWrapper);

        return NoteStatsVO.builder()
                .notes(notesCount.intValue())
                .likes(likesCount.intValue())
                .favorites(favoritesCount.intValue())
                .build();
    }

    @Override
    public IPage<MyNoteVO> queryMyNotes(Long userId, NoteQueryDTO queryDTO) {
        Page<Note> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 1. 先处理标签筛选（获取符合条件的笔记ID）
        List<Long> tagFilteredNoteIds = getNoteIdsByTags(queryDTO.getTagIds());

        // 2. 构建查询条件
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getDelFlag, 0);

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Note::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(Note::getStatus, 1); // 默认查正常
        }

        // 关键词搜索
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Note::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Note::getContent, queryDTO.getKeyword())
            );
        }

        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Note::getCategoryId, queryDTO.getCategoryId());
        }

        // 标签筛选（如果标签筛选有结果，则限制在这些笔记ID中）
        if (tagFilteredNoteIds != null && !tagFilteredNoteIds.isEmpty()) {
            wrapper.in(Note::getNoteId, tagFilteredNoteIds);
        } else if (queryDTO.getTagIds() != null && !queryDTO.getTagIds().isEmpty()) {
            // 有标签条件但没匹配到任何笔记，直接返回空
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
        }

        // 时间范围
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Note::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Note::getCreateTime, queryDTO.getEndTime());
        }

        // 排序
        applyOrderBy(wrapper, queryDTO.getOrderBy(), queryDTO.getOrderDirection());

        // 3. 执行查询
        IPage<Note> notePage = noteMapper.selectPage(page, wrapper);

        // 4. 转换VO
        List<MyNoteVO> records = notePage.getRecords().stream()
                .map(note -> MyNoteVO.builder()
                        .noteId(note.getNoteId())
                        .title(note.getTitle())
                        .updateTime(note.getUpdateTime())
                        .viewCount(note.getViewCount())
                        .isPublic(note.getIsPublic())
                        .build())
                .collect(Collectors.toList());

        IPage<MyNoteVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), notePage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public IPage<FavoriteNoteVO> queryFavorites(Long userId, NoteQueryDTO queryDTO) {
        // 1. 先获取用户收藏的笔记ID（分页）
        Page<NoteReaction> reactionPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<NoteReaction> reactionWrapper = new LambdaQueryWrapper<>();
        reactionWrapper.eq(NoteReaction::getUserId, userId)
                .eq(NoteReaction::getIsFavorite, 1)
                .orderByDesc(NoteReaction::getUpdateTime);

        IPage<NoteReaction> reactionIPage = noteReactionMapper.selectPage(reactionPage, reactionWrapper);

        if (reactionIPage.getRecords().isEmpty()) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
        }

        // 2. 获取收藏的笔记ID列表
        List<Long> favoriteNoteIds = reactionIPage.getRecords().stream()
                .map(NoteReaction::getNoteId)
                .collect(Collectors.toList());

        // 3. 构建笔记查询条件（在收藏的笔记中搜索）
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getNoteId, favoriteNoteIds)
                .eq(Note::getDelFlag, 0)
                .eq(Note::getStatus, 1);  // 收藏的笔记必须是已发布的

        // 关键词搜索
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            noteWrapper.and(w -> w
                    .like(Note::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Note::getContent, queryDTO.getKeyword())
            );
        }

        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            noteWrapper.eq(Note::getCategoryId, queryDTO.getCategoryId());
        }

        // 标签筛选
        List<Long> tagFilteredNoteIds = getNoteIdsByTags(queryDTO.getTagIds());
        if (tagFilteredNoteIds != null && !tagFilteredNoteIds.isEmpty()) {
            noteWrapper.in(Note::getNoteId, tagFilteredNoteIds);
        } else if (queryDTO.getTagIds() != null && !queryDTO.getTagIds().isEmpty()) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
        }

        // 时间范围
        if (queryDTO.getStartTime() != null) {
            noteWrapper.ge(Note::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            noteWrapper.le(Note::getCreateTime, queryDTO.getEndTime());
        }

        // 排序（保持收藏的排序，或者按笔记时间）
        noteWrapper.orderByDesc(Note::getUpdateTime);

        // 4. 查询笔记详情
        List<Note> notes = noteMapper.selectList(noteWrapper);
        Map<Long, Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getNoteId, n -> n));

        // 5. 组装结果（保持收藏表的原始顺序）
        List<FavoriteNoteVO> records = reactionIPage.getRecords().stream()
                .map(reaction -> {
                    Note note = noteMap.get(reaction.getNoteId());
                    if (note == null) return null;
                    return FavoriteNoteVO.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .updateTime(note.getUpdateTime())
                            .viewCount(note.getViewCount())
                            .isPublic(note.getIsPublic())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        IPage<FavoriteNoteVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), reactionIPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public IPage<LikedNoteVO> queryLiked(Long userId, NoteQueryDTO queryDTO) {
        // 逻辑与 queryFavorites 类似，只是改为 attitude = 1
        Page<NoteReaction> reactionPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<NoteReaction> reactionWrapper = new LambdaQueryWrapper<>();
        reactionWrapper.eq(NoteReaction::getUserId, userId)
                .eq(NoteReaction::getAttitude, 1)
                .orderByDesc(NoteReaction::getUpdateTime);

        IPage<NoteReaction> reactionIPage = noteReactionMapper.selectPage(reactionPage, reactionWrapper);

        if (reactionIPage.getRecords().isEmpty()) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
        }

        List<Long> likedNoteIds = reactionIPage.getRecords().stream()
                .map(NoteReaction::getNoteId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getNoteId, likedNoteIds)
                .eq(Note::getDelFlag, 0)
                .eq(Note::getStatus, 1);

        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            noteWrapper.and(w -> w
                    .like(Note::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Note::getContent, queryDTO.getKeyword())
            );
        }

        if (queryDTO.getCategoryId() != null) {
            noteWrapper.eq(Note::getCategoryId, queryDTO.getCategoryId());
        }

        List<Long> tagFilteredNoteIds = getNoteIdsByTags(queryDTO.getTagIds());
        if (tagFilteredNoteIds != null && !tagFilteredNoteIds.isEmpty()) {
            noteWrapper.in(Note::getNoteId, tagFilteredNoteIds);
        } else if (queryDTO.getTagIds() != null && !queryDTO.getTagIds().isEmpty()) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
        }

        if (queryDTO.getStartTime() != null) {
            noteWrapper.ge(Note::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            noteWrapper.le(Note::getCreateTime, queryDTO.getEndTime());
        }

        noteWrapper.orderByDesc(Note::getUpdateTime);

        List<Note> notes = noteMapper.selectList(noteWrapper);
        Map<Long, Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getNoteId, n -> n));

        List<LikedNoteVO> records = reactionIPage.getRecords().stream()
                .map(reaction -> {
                    Note note = noteMap.get(reaction.getNoteId());
                    if (note == null) return null;
                    return LikedNoteVO.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .updateTime(note.getUpdateTime())
                            .viewCount(note.getViewCount())
                            .isPublic(note.getIsPublic())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        IPage<LikedNoteVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), reactionIPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据标签ID列表获取笔记ID（AND逻辑）
     */
    private List<Long> getNoteIdsByTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        String tagIdsStr = tagIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return noteTagRelMapper.selectNoteIdsByTagIds(tagIdsStr, tagIds.size());
    }

    /**
     * 应用排序条件
     */
    private void applyOrderBy(LambdaQueryWrapper<Note> wrapper, String orderBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);
        switch (orderBy) {
            case "createTime":
                if (isAsc) wrapper.orderByAsc(Note::getCreateTime);
                else wrapper.orderByDesc(Note::getCreateTime);
                break;
            case "viewCount":
                if (isAsc) wrapper.orderByAsc(Note::getViewCount);
                else wrapper.orderByDesc(Note::getViewCount);
                break;
            default:  // updateTime
                if (isAsc) wrapper.orderByAsc(Note::getUpdateTime);
                else wrapper.orderByDesc(Note::getUpdateTime);
                break;
        }
    }

    private List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> allIds = new ArrayList<>();
        allIds.add(categoryId);

        // 递归查询所有子分类 ID
        // 这里的 categoryApi 或 categoryMapper 需要根据你的实际注入来使用
        // 假设你已经注入了 CategoryMapper (如果没有，请在类顶部声明并注入)
        fetchChildIdsRecursive(categoryId, allIds);

        return allIds;
    }

    private void fetchChildIdsRecursive(Long parentId, List<Long> allIds) {
        // 查询 parent_id 等于当前 ID 的所有子类
        // 注意：这里需要你根据实际情况注入 sysCategoryMapper
        LambdaQueryWrapper<SysCategory> query = new LambdaQueryWrapper<>();
        query.eq(SysCategory::getParentId, parentId).select(SysCategory::getCategoryId);

        List<Long> childIds = sysCategoryMapper.selectList(query)
                .stream()
                .map(SysCategory::getCategoryId)
                .collect(Collectors.toList());

        if (!childIds.isEmpty()) {
            allIds.addAll(childIds);
            // 继续递归查询更深层级
            for (Long childId : childIds) {
                fetchChildIdsRecursive(childId, allIds);
            }
        }
    }
}