package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.note.domain.dto.AdminNoteQueryDTO;
import com.littlewin.note.domain.dto.NoteAuditDTO;
import com.littlewin.note.domain.vo.AdminNoteVO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.service.AdminNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notes")
public class AdminNoteController {

    private final AdminNoteService adminNoteService;

    /** 管理端笔记列表 */
    @GetMapping("/list")
    public Result<IPage<AdminNoteVO>> list(AdminNoteQueryDTO queryDTO) {
        return Result.success(adminNoteService.listNotes(queryDTO));
    }

    /** 管理端笔记详情 */
    @GetMapping("/{id}")
    public Result<NoteDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(adminNoteService.getNoteDetail(id));
    }

    /** 审核笔记（上架/下架） */
    @PutMapping("/{id}/audit")
    @Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "审核笔记")
    public Result<Void> audit(@PathVariable("id") Long id,
                              @RequestBody @Valid NoteAuditDTO dto) {
        adminNoteService.auditNote(id, dto.getStatus());
        LogContext.setBusinessId(id);
        LogContext.setDesc((dto.getStatus() != null && dto.getStatus() == 1) ? "上架笔记: " + id : "下架笔记: " + id);
        return Result.success(null);
    }

    /** 管理员强制删除笔记 */
    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "管理员强制删除笔记")
    public Result<Void> delete(@PathVariable("id") Long id) {
        adminNoteService.forceDelete(id);
        LogContext.setBusinessId(id);
        LogContext.setDesc("强制删除笔记: " + id);
        return Result.success(null);
    }
}
