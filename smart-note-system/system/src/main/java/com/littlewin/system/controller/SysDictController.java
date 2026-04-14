package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;
import com.littlewin.system.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sys/dict")
public class SysDictController {

    @Autowired
    private SysDictService dictService;

    /**
     * 根据字典类型查询字典数据
     * 给 Geeker-admin 表单下拉框使用
     */
    @GetMapping("/type/{dictType}")
    public Result<List<SysDictData>> dictData(@PathVariable("dictType") String dictType) {
        List<SysDictData> list = dictService.selectDictDataByType(dictType);
        return Result.success(list);
    }

    /**
     * 查询所有字典类型
     * 给后台字典管理页面使用
     */
    @GetMapping("/type/list")
    public Result<List<SysDictType>> typeList() {
        List<SysDictType> list = dictService.selectDictTypeList();
        return Result.success(list);
    }
}