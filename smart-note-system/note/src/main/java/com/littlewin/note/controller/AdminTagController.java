package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.note.domain.dto.AdminTagQueryDTO;
import com.littlewin.note.domain.vo.AdminTagVO;
import com.littlewin.note.service.AdminTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagService adminTagService;

    @GetMapping("/list")
    public Result<IPage<AdminTagVO>> list(AdminTagQueryDTO queryDTO) {
        return Result.success(adminTagService.listTags(queryDTO));
    }

    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "管理员删除标签")
    public Result<Void> delete(@PathVariable("id") Long id) {
        adminTagService.deleteTag(id);
        LogContext.setBusinessId(id);
        LogContext.setDesc("删除标签: " + id);
        return Result.success(null);
    }
}
