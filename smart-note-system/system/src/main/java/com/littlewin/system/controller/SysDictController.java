package com.littlewin.system.controller;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.DictDataQueryDTO;
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
     * 分页查询字典类型
     * 检索条件：字典名称、字典状态
     */
    @GetMapping("/type/list")
    public Result<IPage<SysDictType>> typeList(DictTypeQueryDTO queryDTO) {
        // 统一风格：调用 service 获取分页对象
        return Result.success(dictService.selectDictTypePage(queryDTO));
    }

    /**
     * 修改字典类型(支持全量编辑和局部状态切换)
     */
    @PutMapping("/type/edit")
    public Result edit(@RequestBody SysDictType dictType) {
        // 1. 强制校验主键，防止全表更新
        if (dictType.getDictId() == null) {
            return Result.error("操作失败：字典ID不能为空");
        }

        // 2. 执行更新
        // 如果前端只传 { "dictId": 1, "status": 0 }，
        boolean success = dictService.updateDictType(dictType);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/type/add")
    public Result add(@RequestBody SysDictType dictType) {
        return dictService.insertDictType(dictType) ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 删除字典类型
     * 支持单条删除和批量删除：/api/admin/sys/dict/type/delete?ids=1,2,3
     */
    @DeleteMapping("/type/delete")
    public Result remove(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的数据");
        }
        return dictService.deleteDictTypeByIds(ids) ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 分页查询字典数据项
     */
    @GetMapping("/data/list")
    public Result<IPage<SysDictData>> dataList(DictDataQueryDTO queryDTO) {
        return Result.success(dictService.selectDictDataPage(queryDTO));
    }

    /**
     * 新增字典数据项
     */
    @PostMapping("/data/add")
    public Result addData(@RequestBody SysDictData dictData) {
        return dictService.insertDictData(dictData) ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改字典数据项
     */
    @PutMapping("/data/edit")
    public Result editData(@RequestBody SysDictData dictData) {
        if (dictData.getDataId() == null) {
            return Result.error("操作失败：数据ID不能为空");
        }
        return dictService.updateDictData(dictData) ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除字典数据项
     */
    @DeleteMapping("/data/delete")
    public Result removeData(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的数据");
        }
        return dictService.deleteDictDataByIds(ids) ? Result.success("删除成功") : Result.error("删除失败");
    }



}