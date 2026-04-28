package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.domain.vo.InteractionResultVO;
import com.littlewin.note.domain.vo.InteractionStatusVO;
import com.littlewin.note.mapper.NoteReactionMapper;
import com.littlewin.note.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 互动服务实现：点赞/收藏切换、状态查询
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final NoteReactionMapper noteReactionMapper;

    /**
     * 点赞/收藏切换入口
     * 根据 type 分发到 toggleLike 或 toggleCollect
     */
    @Override
    public InteractionResultVO toggle(Long userId, Long noteId, String type) {
        NoteReaction existing = noteReactionMapper.selectOne(
                new LambdaQueryWrapper<NoteReaction>()
                        .eq(NoteReaction::getNoteId, noteId)
                        .eq(NoteReaction::getUserId, userId)
        );

        if ("like".equals(type)) {
            return toggleLike(userId, noteId, existing);
        } else if ("collect".equals(type)) {
            return toggleCollect(userId, noteId, existing);
        } else {
            throw new ServiceException("不支持的互动类型: " + type);
        }
    }

    /** 点赞切换：无记录→插入attitude=1；attitude=1→改0(取消)；attitude=0→改1 */
    private InteractionResultVO toggleLike(Long userId, Long noteId, NoteReaction existing) {
        boolean isLiked;
        if (existing == null) {
            NoteReaction reaction = new NoteReaction();
            reaction.setNoteId(noteId);
            reaction.setUserId(userId);
            reaction.setAttitude(1);
            reaction.setIsFavorite(0);
            reaction.setCreateTime(LocalDateTime.now());
            reaction.setUpdateTime(LocalDateTime.now());
            try {
                noteReactionMapper.insert(reaction);
            } catch (DuplicateKeyException e) {
                throw new ServiceException("操作过于频繁，请稍后再试");
            }
            isLiked = true;
        } else if (existing.getAttitude() != null && existing.getAttitude() == 1) {
            existing.setAttitude(0);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            isLiked = false;
        } else {
            existing.setAttitude(1);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            isLiked = true;
        }

        Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionResultVO.builder()
                .isLiked(isLiked)
                .isCollected(existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1)
                .likeCount(likeCount.intValue())
                .collectCount(collectCount.intValue())
                .build();
    }

    /** 收藏切换：无记录→插入isFavorite=1；isFavorite=1→改0(取消)；isFavorite=0→改1 */
    private InteractionResultVO toggleCollect(Long userId, Long noteId, NoteReaction existing) {
        boolean isCollected;
        if (existing == null) {
            NoteReaction reaction = new NoteReaction();
            reaction.setNoteId(noteId);
            reaction.setUserId(userId);
            reaction.setAttitude(0);
            reaction.setIsFavorite(1);
            reaction.setCreateTime(LocalDateTime.now());
            reaction.setUpdateTime(LocalDateTime.now());
            try {
                noteReactionMapper.insert(reaction);
            } catch (DuplicateKeyException e) {
                throw new ServiceException("操作过于频繁，请稍后再试");
            }
            isCollected = true;
        } else if (existing.getIsFavorite() != null && existing.getIsFavorite() == 1) {
            existing.setIsFavorite(0);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            isCollected = false;
        } else {
            existing.setIsFavorite(1);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            isCollected = true;
        }

        Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionResultVO.builder()
                .isLiked(existing != null && existing.getAttitude() != null && existing.getAttitude() == 1)
                .isCollected(isCollected)
                .likeCount(likeCount.intValue())
                .collectCount(collectCount.intValue())
                .build();
    }

    /** 查询单条笔记的当前用户互动状态 + 聚合计数 */
    @Override
    public InteractionStatusVO getStatus(Long userId, Long noteId) {
        NoteReaction existing = noteReactionMapper.selectOne(
                new LambdaQueryWrapper<NoteReaction>()
                        .eq(NoteReaction::getNoteId, noteId)
                        .eq(NoteReaction::getUserId, userId)
        );

        boolean isLiked = existing != null && existing.getAttitude() != null && existing.getAttitude() == 1;
        boolean isCollected = existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1;

        Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionStatusVO.builder()
                .isLiked(isLiked)
                .isCollected(isCollected)
                .likeCount(likeCount.intValue())
                .collectCount(collectCount.intValue())
                .build();
    }

    /** 批量查询：一次IN查出所有reaction记录，再逐条聚合计数 */
    @Override
    public Map<String, InteractionStatusVO> batchGetStatus(Long userId, List<Long> noteIds) {
        Map<String, InteractionStatusVO> result = new HashMap<>();

        if (noteIds == null || noteIds.isEmpty()) {
            return result;
        }

        List<NoteReaction> reactions = noteReactionMapper.selectList(
                new LambdaQueryWrapper<NoteReaction>()
                        .eq(NoteReaction::getUserId, userId)
                        .in(NoteReaction::getNoteId, noteIds)
        );

        Map<Long, NoteReaction> reactionMap = new HashMap<>();
        for (NoteReaction r : reactions) {
            reactionMap.put(r.getNoteId(), r);
        }

        for (Long noteId : noteIds) {
            NoteReaction existing = reactionMap.get(noteId);
            boolean isLiked = existing != null && existing.getAttitude() != null && existing.getAttitude() == 1;
            boolean isCollected = existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1;

            Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);
            Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

            result.put(String.valueOf(noteId), InteractionStatusVO.builder()
                    .isLiked(isLiked)
                    .isCollected(isCollected)
                    .likeCount(likeCount.intValue())
                    .collectCount(collectCount.intValue())
                    .build());
        }

        return result;
    }
}
