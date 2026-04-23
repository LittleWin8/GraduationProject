package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.note.domain.entity.Note;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface NoteMapper extends BaseMapper<Note> {

    // MyBatis-Plus 自带 CRUD 方法已够用，无需额外定义
}