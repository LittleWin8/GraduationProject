package com.littlewin.note.service;

import com.littlewin.note.domain.vo.NoteDetailVO;

public interface NoteDetailService {
    /**
     * 获取笔记详情（公开笔记或本人笔记）
     */
    NoteDetailVO getNoteDetail(Long noteId, Long userId);
}
