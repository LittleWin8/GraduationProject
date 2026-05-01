package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.dto.CommentCreateDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteComment;
import com.littlewin.note.domain.vo.CommentVO;
import com.littlewin.note.mapper.NoteCommentMapper;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.service.CommentService;
import com.littlewin.note.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final NoteCommentMapper noteCommentMapper;
    private final NoteMapper noteMapper;
    private final MessageService messageService;

    /** 分页查询评论列表（关联sys_user查作者信息） */
    @Override
    public IPage<CommentVO> listComments(Long noteId, Long userId, int page, int size) {
        Page<CommentVO> p = new Page<>(page, size);
        return noteCommentMapper.selectCommentPage(p, noteId, userId);
    }

    /** 发表评论，返回完整评论信息 */
    @Override
    public CommentVO createComment(Long userId, CommentCreateDTO dto) {
        if (dto.getNoteId() == null) {
            throw new ServiceException("笔记ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ServiceException("评论内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            throw new ServiceException("评论内容不能超过500字");
        }

        NoteComment comment = new NoteComment();
        comment.setNoteId(dto.getNoteId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setCreateTime(LocalDateTime.now());
        comment.setDelFlag(0);
        noteCommentMapper.insert(comment);
        noteMapper.addCommentCount(dto.getNoteId(), 1L);

        // 评论时给笔记作者发站内消息（自己评论自己的笔记不发）
        Note note = noteMapper.selectById(dto.getNoteId());
        if (note != null && !note.getUserId().equals(userId)) {
            int type = dto.getParentId() != null ? 2 : 1;
            String summary = dto.getContent().length() > 50
                    ? dto.getContent().substring(0, 50) : dto.getContent();
            messageService.sendMessage(note.getUserId(), userId, dto.getNoteId(),
                    comment.getCommentId(), type, summary);
        }

        CommentVO vo = noteCommentMapper.selectCommentById(comment.getCommentId(), userId);
        if (vo != null) {
            return vo;
        }

        return CommentVO.builder()
                .commentId(comment.getCommentId())
                .noteId(comment.getNoteId())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .createTime(comment.getCreateTime())
                .isOwner(true)
                .build();
    }

    /** 逻辑删除评论（仅本人可删） */
    @Override
    public void deleteComment(Long userId, Long commentId) {
        NoteComment comment = noteCommentMapper.selectById(commentId);
        if (comment == null || comment.getDelFlag() == 1) {
            throw new ServiceException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException("无权删除他人评论");
        }
        comment.setDelFlag(1);
        noteCommentMapper.updateById(comment);
        noteMapper.addCommentCount(comment.getNoteId(), -1L);
    }
}
