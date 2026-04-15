package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
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
     * 分页查询字典类型
     * 检索条件：字典名称、字典状态
     */
    @GetMapping("/type/list")
    public Result<IPage<SysDictType>> typeList(DictTypeQueryDTO queryDTO) {
        // 统一风格：调用 service 获取分页对象
        return Result.success(dictService.selectDictTypePage(queryDTO));
    }

}