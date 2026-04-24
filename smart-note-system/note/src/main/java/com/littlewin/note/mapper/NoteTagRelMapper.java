package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.note.domain.entity.NoteTagRel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface NoteTagRelMapper extends BaseMapper<NoteTagRel> {

    /**
     * 根据标签ID列表查询笔记ID（AND逻辑：包含所有指定标签）
     */
    @Select("SELECT note_id FROM note_tag_rel WHERE tag_id IN (${tagIds}) " +
            "GROUP BY note_id HAVING COUNT(DISTINCT tag_id) = #{tagCount}")
    List<Long> selectNoteIdsByTagIds(@Param("tagIds") String tagIds,
                                     @Param("tagCount") int tagCount);
}