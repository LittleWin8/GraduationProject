package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.service.NoteDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/notes")
public class WxNoteController {

    private final NoteDetailService noteDetailService;

    @GetMapping("/{id}")
    public Result<NoteDetailVO> getNoteDetail(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteDetailService.getNoteDetail(id, currentUserId));
    }
}
