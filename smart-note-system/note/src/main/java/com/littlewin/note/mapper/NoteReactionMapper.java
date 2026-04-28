package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.NoteReaction;
import com.littlewin.note.domain.vo.FavoriteNoteVO;
import com.littlewin.note.domain.vo.LikedNoteVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NoteReactionMapper extends BaseMapper<NoteReaction> {

    Long countLikesByUserNotes(@Param("userId") Long userId);

    IPage<FavoriteNoteVO> selectFavoriteNotePage(Page<FavoriteNoteVO> page,
                                                 @Param("userId") Long userId,
                                                 @Param("query") NoteQueryDTO query,
                                                 @Param("categoryIds") List<Long> categoryIds);

    IPage<LikedNoteVO> selectLikedNotePage(Page<LikedNoteVO> page,
                                           @Param("userId") Long userId,
                                           @Param("query") NoteQueryDTO query,
                                           @Param("categoryIds") List<Long> categoryIds);

    /** 统计指定笔记的点赞数 */
    Long countLikesByNoteId(@Param("noteId") Long noteId);

    /** 统计指定笔记的收藏数 */
    Long countCollectsByNoteId(@Param("noteId") Long noteId);
}