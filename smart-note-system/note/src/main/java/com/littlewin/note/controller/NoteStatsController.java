package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.vo.MyNoteVO;
import com.littlewin.note.domain.vo.NoteStatsVO;
import com.littlewin.note.service.NoteStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wx/note")
@RequiredArgsConstructor
public class NoteStatsController {

    private final NoteStatsService noteStatsService;

    @GetMapping("/stats")
    public Result<NoteStatsVO> getStats() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        NoteStatsVO stats = noteStatsService.getUserStats(userId);
        return Result.success(stats);
    }

    @GetMapping("/my-notes")
    public Result<IPage<MyNoteVO>> getMyNotes(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        IPage<MyNoteVO> page = noteStatsService.getMyNotes(userId, pageNum, pageSize);
        return Result.success(page);
    }
}