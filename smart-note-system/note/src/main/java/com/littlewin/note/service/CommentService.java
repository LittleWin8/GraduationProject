package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.CommentCreateDTO;
import com.littlewin.note.domain.vo.CommentVO;

/**
 * 评论服务接口（列表查询、发表、删除）
 */
public interface CommentService {

    /** 分页查询评论列表 */
    IPage<CommentVO> listComments(Long noteId, Long userId, int page, int size);

    /** 发表评论，返回完整评论信息 */
    CommentVO createComment(Long userId, CommentCreateDTO dto);

    /** 逻辑删除评论（仅本人可删） */
    void deleteComment(Long userId, Long commentId);
}
