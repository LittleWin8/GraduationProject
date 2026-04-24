package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.note.domain.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
    /**
     * 查询用户的所有标签，并统计每个标签关联的笔记数量
     */
    List<NoteTag> selectTagListWithCount(Long userId);
}