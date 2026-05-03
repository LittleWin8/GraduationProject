package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.note.domain.dto.CategoryDTO;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    /** 分类列表（树形/扁平），支持 parentId 和 status 筛选 */
    @GetMapping("/list")
    public Result<List<SysCategory>> list(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(adminCategoryService.listCategories(parentId, status));
    }

    /** 新增分类 */
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "新增分类")
    public Result<Void> add(@RequestBody CategoryDTO dto) {
        adminCategoryService.addCategory(dto);
        LogContext.setDesc("新增分类: " + dto.getName());
        return Result.success(null);
    }

    /** 修改分类 */
    @PutMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "修改分类")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody CategoryDTO dto) {
        adminCategoryService.updateCategory(id, dto);
        LogContext.setBusinessId(id);
        LogContext.setDesc("修改分类: " + id);
        return Result.success(null);
    }

    /** 删除分类 */
    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "删除分类")
    public Result<Void> delete(@PathVariable("id") Long id) {
        adminCategoryService.deleteCategory(id);
        LogContext.setBusinessId(id);
        LogContext.setDesc("删除分类: " + id);
        return Result.success(null);
    }

    /** 切换分类状态 */
    @PutMapping("/{id}/status")
    @Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "切换分类状态")
    public Result<Void> toggleStatus(@PathVariable("id") Long id) {
        adminCategoryService.toggleStatus(id);
        LogContext.setBusinessId(id);
        LogContext.setDesc("切换分类状态: " + id);
        return Result.success(null);
    }
}
