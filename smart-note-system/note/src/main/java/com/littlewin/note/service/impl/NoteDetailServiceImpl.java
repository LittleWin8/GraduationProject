package com.littlewin.note.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.service.NoteDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteDetailServiceImpl implements NoteDetailService {

    private final NoteMapper noteMapper;

    @Override
    public NoteDetailVO getNoteDetail(Long noteId, Long userId) {
        NoteDetailVO detail = noteMapper.selectNoteDetailById(noteId, userId);
        if (detail == null) {
            throw new ServiceException("笔记不存在或无权限访问");
        }
        return detail;
    }
}
