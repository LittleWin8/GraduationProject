package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.vo.MyNoteVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NoteMapper extends BaseMapper<Note> {

    IPage<MyNoteVO> selectMyNoteVOPage(Page<Note> page,
                                       @Param("userId") Long userId,
                                       @Param("query") NoteQueryDTO query,
                                       @Param("categoryIds") List<Long> categoryIds);
}