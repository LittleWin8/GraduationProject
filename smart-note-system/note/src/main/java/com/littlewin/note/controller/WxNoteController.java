package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.NoteCreateDTO;
import com.littlewin.note.domain.dto.NoteQueryDTO;
import com.littlewin.note.domain.vo.NoteDetailVO;
import com.littlewin.note.domain.vo.NoteListVO;
import com.littlewin.note.service.WxNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序端笔记接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/notes")
public class WxNoteController {

    private final WxNoteService noteDetailService;

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public Result<NoteDetailVO> getNoteDetail(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteDetailService.getNoteDetail(id, currentUserId));
    }

    /**
     * 创建笔记
     */
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "创建笔记")
    public Result<Map<String, Object>> createNote(@RequestBody NoteCreateDTO dto) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        Map<String, Object> result = noteDetailService.createNote(dto, currentUserId);
        LogContext.setBusinessId((Long) result.get("noteId"));
        LogContext.setDesc("创建笔记: " + dto.getTitle());
        return Result.success(result);
    }

    /**
     * 笔记列表（公开社区 / 我的笔记）
     *
     * @param type     查询类型：public-公开社区笔记，my-我的笔记
     * @param queryDTO 分页及筛选条件（categoryId、tagIds、status、keyword等）
     */
    @GetMapping
    public Result<IPage<NoteListVO>> listNotes(
            @RequestParam(value = "type", defaultValue = "public") String type,
            NoteQueryDTO queryDTO) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(noteDetailService.listNotes(currentUserId, type, queryDTO));
    }

    /**
     * 更新笔记
     *
     * @param id  笔记ID
     * @param dto 更新内容（title、content、categoryId、isPublic、tagIds）
     */
    @PutMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "更新笔记")
    public Result<?> updateNote(@PathVariable("id") Long id,
                                @RequestBody NoteCreateDTO dto) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        noteDetailService.updateNote(id, dto, currentUserId);
        LogContext.setBusinessId(id);
        LogContext.setDesc("更新笔记: " + dto.getTitle());
        return Result.success();
    }

    /**
     * 删除笔记
     *
     * @param id        笔记ID
     * @param permanent false(默认)-移入回收站(status=2)，true-永久删除(del_flag=1)
     */
    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "删除笔记")
    public Result<?> deleteNote(@PathVariable("id") Long id,
                                @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        noteDetailService.deleteNote(id, permanent, currentUserId);
        LogContext.setBusinessId(id);
        LogContext.setDesc(permanent ? "永久删除笔记: " + id : "移入回收站: " + id);
        return Result.success();
    }

    /**
     * 恢复笔记（从回收站恢复为正常状态）
     *
     * @param id 笔记ID
     */
    @PutMapping("/{id}/restore")
    @Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "恢复笔记")
    public Result<?> restoreNote(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        noteDetailService.restoreNote(id, currentUserId);
        LogContext.setBusinessId(id);
        LogContext.setDesc("恢复笔记: " + id);
        return Result.success();
    }
}
