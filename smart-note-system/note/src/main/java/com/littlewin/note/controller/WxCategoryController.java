package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 注意这里换成了 QueryWrapper
import com.littlewin.common.core.Result;
import com.littlewin.common.utils.TreeUtils;
import com.littlewin.note.domain.entity.SysCategory;
import com.littlewin.note.mapper.SysCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wx/categories")
@RequiredArgsConstructor
public class WxCategoryController {
    private final SysCategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result<List<SysCategory>> getActiveCategoryTree() {
        // 1. 使用普通的 QueryWrapper，直接写数据库列名
        QueryWrapper<SysCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)              // 对应数据库 status 列
                .orderByAsc("sort_order");    // 对应数据库 sort_order 列

        List<SysCategory> activeList = categoryMapper.selectList(wrapper);

        // 2. 构建树形结构
        List<SysCategory> tree = TreeUtils.build(activeList, 0L);
        return Result.success(tree);
    }
}