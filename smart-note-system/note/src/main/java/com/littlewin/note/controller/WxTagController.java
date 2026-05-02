package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.domain.vo.TagNoteVO;
import com.littlewin.note.service.NoteTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/tags")
public class WxTagController {

    private final NoteTagService tagService;

    // 获取我的标签列表
    @GetMapping
    public Result<List<NoteTag>> getMyTags() {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        List<NoteTag> tags = tagService.listMyTags(currentUserId);
        return Result.success(tags);
    }

    // 创建标签
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "创建标签")
    public Result<NoteTag> createTag(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException("标签名称不能为空");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        NoteTag newTag = tagService.saveTag(name.trim(), currentUserId);
        LogContext.setBusinessId(newTag.getTagId());
        LogContext.setDesc("创建标签: " + newTag.getName());
        return Result.success(newTag);
    }

    // 删除标签
    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "删除标签")
    public Result<?> deleteTag(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        tagService.removeTag(id, currentUserId);
        LogContext.setBusinessId(id);
        LogContext.setDesc("删除标签: " + id);
        return Result.success();
    }

    // 标签下的笔记列表（分页）
    @GetMapping("/{id}/notes")
    public Result<IPage<TagNoteVO>> getNotesByTag(@PathVariable("id") Long id,
                                                  @RequestParam(value = "page", defaultValue = "1") long page,
                                                  @RequestParam(value = "size", defaultValue = "10") long size) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        IPage<TagNoteVO> result = tagService.listNotesByTag(id, currentUserId, page, size);
        return Result.success(result);
    }
}