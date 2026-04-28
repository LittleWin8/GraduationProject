package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.entity.NoteComment;
import com.littlewin.note.domain.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

/**
 * 笔记评论Mapper
 */
public interface NoteCommentMapper extends BaseMapper<NoteComment> {

    /** 分页查询评论列表（关联sys_user查作者信息） */
    IPage<CommentVO> selectCommentPage(Page<CommentVO> page,
                                       @Param("noteId") Long noteId,
                                       @Param("userId") Long userId);

    /** 按commentId查询单条评论（关联sys_user） */
    CommentVO selectCommentById(@Param("commentId") Long commentId,
                                @Param("userId") Long userId);
}
