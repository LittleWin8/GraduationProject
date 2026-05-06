package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.AdminNoteQueryDTO;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.vo.AdminNoteVO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.service.AdminNoteService;
import com.littlewin.note.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminNoteServiceImpl implements AdminNoteService {

    private final NoteMapper noteMapper;
    private final MessageService messageService;

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
            throw new ServiceException("状态只能是 1（上架）或 3（下架）");
        }

        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDelFlag() == 1) {
            throw new ServiceException("笔记不存在或已删除");
        }

        int rows = noteMapper.auditNote(noteId, status);
        if (rows == 0) {
            throw new ServiceException("笔记不存在或已删除");
        }

        Long adminUserId = SecurityUtils.getLoginUser().getUserId();
        if (status == 1) {
            messageService.sendMessage(
                    note.getUserId(), adminUserId, noteId, null,
                    3, "审核通过", "你的笔记「" + note.getTitle() + "」已通过审核，已上架展示"
            );
        } else {
            messageService.sendMessage(
                    note.getUserId(), adminUserId, noteId, null,
                    4, "审核不通过", "你的笔记「" + note.getTitle() + "」未通过审核，已被下架"
            );
        }
    }

    @Override
    public void reviewNote(Long noteId) {
        int rows = noteMapper.reviewNote(noteId);
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
