package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.AdminNoteQueryDTO;
import com.littlewin.note.domain.vo.AdminNoteVO;
import com.littlewin.note.domain.vo.NoteDetailVO;

public interface AdminNoteService {

    IPage<AdminNoteVO> listNotes(AdminNoteQueryDTO queryDTO);

    NoteDetailVO getNoteDetail(Long noteId);

    void auditNote(Long noteId, Integer status);

    void forceDelete(Long noteId);
}
