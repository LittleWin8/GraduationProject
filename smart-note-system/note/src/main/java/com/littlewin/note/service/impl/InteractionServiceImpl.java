package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.domain.vo.InteractionResultVO;
import com.littlewin.note.domain.vo.InteractionStatusVO;
import com.littlewin.note.mapper.NoteMapper;
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
    private final NoteMapper noteMapper;

    private InteractionResultVO buildResultFromExisting(NoteReaction existing, Long noteId) {
        boolean isLiked = existing != null && existing.getAttitude() != null && existing.getAttitude() == 1;
        boolean isCollected = existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1;
        Note note = noteMapper.selectById(noteId);
        int likeCount = note != null && note.getLikeCount() != null ? note.getLikeCount() : 0;
        int collectCount = noteReactionMapper.countCollectsByNoteId(noteId).intValue();
        return InteractionResultVO.builder()
                .isLiked(isLiked)
                .isCollected(isCollected)
                .likeCount(likeCount)
                .collectCount(collectCount)
                .build();
    }

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
                existing = noteReactionMapper.selectOne(
                        new LambdaQueryWrapper<NoteReaction>()
                                .eq(NoteReaction::getNoteId, noteId)
                                .eq(NoteReaction::getUserId, userId)
                );
                return buildResultFromExisting(existing, noteId);
            }
            noteMapper.addLikeCount(noteId, 1L);
            isLiked = true;
        } else if (existing.getAttitude() != null && existing.getAttitude() == 1) {
            existing.setAttitude(0);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            noteMapper.addLikeCount(noteId, -1L);
            isLiked = false;
        } else {
            existing.setAttitude(1);
            existing.setUpdateTime(LocalDateTime.now());
            noteReactionMapper.updateById(existing);
            noteMapper.addLikeCount(noteId, 1L);
            isLiked = true;
        }

        Note note = noteMapper.selectById(noteId);
        int likeCount = note != null && note.getLikeCount() != null ? note.getLikeCount() : 0;
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionResultVO.builder()
                .isLiked(isLiked)
                .isCollected(existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1)
                .likeCount(likeCount)
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
                existing = noteReactionMapper.selectOne(
                        new LambdaQueryWrapper<NoteReaction>()
                                .eq(NoteReaction::getNoteId, noteId)
                                .eq(NoteReaction::getUserId, userId)
                );
                return buildResultFromExisting(existing, noteId);
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

        Note note = noteMapper.selectById(noteId);
        int likeCount = note != null && note.getLikeCount() != null ? note.getLikeCount() : 0;
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionResultVO.builder()
                .isLiked(existing != null && existing.getAttitude() != null && existing.getAttitude() == 1)
                .isCollected(isCollected)
                .likeCount(likeCount)
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

        Note note = noteMapper.selectById(noteId);
        int likeCount = note != null && note.getLikeCount() != null ? note.getLikeCount() : 0;
        Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

        return InteractionStatusVO.builder()
                .isLiked(isLiked)
                .isCollected(isCollected)
                .likeCount(likeCount)
                .collectCount(collectCount.intValue())
                .build();
    }

    /**
     * 批量查询：一次IN查出所有reaction记录 + 一条GROUP BY统计点赞/收藏数
     * SQL次数：2次（原来2N+1次），10条笔记从21次降为2次
     */
    @Override
    public Map<String, InteractionStatusVO> batchGetStatus(Long userId, List<Long> noteIds) {
        Map<String, InteractionStatusVO> result = new HashMap<>();

        if (noteIds == null || noteIds.isEmpty()) {
            return result;
        }

        // 1. 一次IN查出当前用户对所有目标笔记的reaction记录
        List<NoteReaction> reactions = noteReactionMapper.selectList(
                new LambdaQueryWrapper<NoteReaction>()
                        .eq(NoteReaction::getUserId, userId)
                        .in(NoteReaction::getNoteId, noteIds)
        );

        Map<Long, NoteReaction> reactionMap = new HashMap<>();
        for (NoteReaction r : reactions) {
            reactionMap.put(r.getNoteId(), r);
        }

        // 2. 批量查询笔记冗余的 like_count + 仍用 GROUP BY 统计收藏数
        List<Note> notes = noteMapper.selectBatchIds(noteIds);
        Map<Long, Integer> noteLikeMap = new HashMap<>();
        for (Note n : notes) {
            noteLikeMap.put(n.getNoteId(), n.getLikeCount() != null ? n.getLikeCount() : 0);
        }
        List<Map<String, Object>> countRows = noteReactionMapper.batchCountByNoteIds(noteIds);
        Map<Long, Integer> collectMap = new HashMap<>();
        for (Map<String, Object> row : countRows) {
            Long nid = ((Number) row.get("note_id")).longValue();
            int collectCnt = ((Number) row.get("collect_count")).intValue();
            collectMap.put(nid, collectCnt);
        }

        // 3. 组装结果
        for (Long noteId : noteIds) {
            NoteReaction existing = reactionMap.get(noteId);
            boolean isLiked = existing != null && existing.getAttitude() != null && existing.getAttitude() == 1;
            boolean isCollected = existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1;

            int likeCount = noteLikeMap.getOrDefault(noteId, 0);
            int collectCount = collectMap.getOrDefault(noteId, 0);

            result.put(String.valueOf(noteId), InteractionStatusVO.builder()
                    .isLiked(isLiked)
                    .isCollected(isCollected)
                    .likeCount(likeCount)
                    .collectCount(collectCount)
                    .build());
        }

        return result;
    }
}
