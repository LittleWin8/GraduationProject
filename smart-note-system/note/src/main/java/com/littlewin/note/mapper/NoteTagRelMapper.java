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
    List<Long> selectNoteIdsByTagIds(@Param("tagIds") List<Long> tagIds,
                                     @Param("tagCount") int tagCount);
}