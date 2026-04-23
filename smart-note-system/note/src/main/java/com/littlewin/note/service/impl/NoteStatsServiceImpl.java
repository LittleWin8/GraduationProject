package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.mapper.NoteReactionMapper;
import com.littlewin.note.service.NoteStatsService;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteStatsServiceImpl implements NoteStatsService {

    private final NoteMapper noteMapper;
    private final NoteReactionMapper noteReactionMapper;

    @Override
    public NoteStatsVO getUserStats(Long userId) {
        // 1. 查询笔记数量（正常状态的笔记，不包括草稿和回收站）
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId)
                .eq(Note::getStatus, 1)  // 1-正常
                .eq(Note::getDelFlag, 0); // 0-未删除
        Long notesCount = noteMapper.selectCount(noteWrapper);

        // 2. 查询获赞数量（用户收到的赞）
        // 方式：查询用户的所有笔记，再统计这些笔记被点赞的总数
        Long likesCount = noteReactionMapper.countLikesByUserNotes(userId);

        // 3. 查询收藏数量（用户收藏的笔记数）
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
    public IPage<MyNoteVO> getMyNotes(Long userId, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);

        // 查询用户的笔记列表（正常状态，按更新时间倒序）
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getStatus, 1)    // 1-正常
                .eq(Note::getDelFlag, 0)   // 0-未删除
                .orderByDesc(Note::getUpdateTime);

        IPage<Note> notePage = noteMapper.selectPage(page, wrapper);

        // 转换为VO
        List<MyNoteVO> records = notePage.getRecords().stream()
                .map(note -> MyNoteVO.builder()
                        .noteId(note.getNoteId())
                        .title(note.getTitle())
                        .updateTime(note.getUpdateTime())
                        .viewCount(note.getViewCount())
                        .isPublic(note.getIsPublic())
                        .build())
                .collect(Collectors.toList());

        IPage<MyNoteVO> resultPage = new Page<>(pageNum, pageSize, notePage.getTotal());
        resultPage.setRecords(records);

        return resultPage;
    }
}