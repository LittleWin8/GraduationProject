package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.entity.NoteTag;
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
    public Result<NoteTag> createTag(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException("标签名称不能为空");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        NoteTag newTag = tagService.saveTag(name.trim(), currentUserId);
        return Result.success(newTag);
    }

    // 删除标签
    @DeleteMapping("/{id}")
    public Result<?> deleteTag(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        tagService.removeTag(id, currentUserId);
        return Result.success();
    }
}