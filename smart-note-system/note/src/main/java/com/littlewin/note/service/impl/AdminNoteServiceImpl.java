package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.dto.AdminNoteQueryDTO;
import com.littlewin.note.domain.vo.AdminNoteVO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.service.AdminNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminNoteServiceImpl implements AdminNoteService {

    private final NoteMapper noteMapper;

    @Override
    public IPage<AdminNoteVO> listNotes(AdminNoteQueryDTO queryDTO) {
        Page<AdminNoteVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return noteMapper.selectAdminNotePage(page, queryDTO);
    }

    @Override
    public NoteDetailVO getNoteDetail(Long noteId) {
        NoteDetailVO vo = noteMapper.selectAdminNoteDetailById(noteId);
        if (vo == null) {
            throw new ServiceException("笔记不存在");
        }
        return vo;
    }

    @Override
    public void auditNote(Long noteId, Integer status) {
        if (status == null || (status != 1 && status != 3)) {
            throw new ServiceException("审核状态只接受1(上架)或3(下架)");
        }
        int rows = noteMapper.auditNote(noteId, status);
        if (rows == 0) {
            throw new ServiceException("笔记不存在或已删除");
        }
    }

    @Override
    public void forceDelete(Long noteId) {
        int rows = noteMapper.adminForceDelete(noteId);
        if (rows == 0) {
            throw new ServiceException("笔记不存在");
        }
    }
}
