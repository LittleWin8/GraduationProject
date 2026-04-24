package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.vo.FavoriteNoteVO;
import com.littlewin.note.domain.vo.LikedNoteVO;
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
        return Result.success(noteStatsService.getUserStats(userId));
    }

    @GetMapping("/my-notes")
    public Result<IPage<MyNoteVO>> getMyNotes(NoteQueryDTO queryDTO) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteStatsService.queryMyNotes(userId, queryDTO));
    }

    @GetMapping("/favorites")
    public Result<IPage<FavoriteNoteVO>> getFavorites(NoteQueryDTO queryDTO) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteStatsService.queryFavorites(userId, queryDTO));
    }

    @GetMapping("/liked")
    public Result<IPage<LikedNoteVO>> getLiked(NoteQueryDTO queryDTO) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteStatsService.queryLiked(userId, queryDTO));
    }
}