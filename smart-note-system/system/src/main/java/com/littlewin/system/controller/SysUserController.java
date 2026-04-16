package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 账户管理后端接口
 * 路径：/api/admin/sys/user
 */
@RestController
@RequestMapping("/api/admin/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 获取用户分页列表
     * 支持根据：用户类型、角色、用户名、性别、城市、创建时间进行筛选
     */
    @GetMapping("/list")
    public Result<IPage<UserListVO>> list(UserQueryDTO queryDTO) {
        return Result.success(sysUserService.getUserPageList(queryDTO));
    }

    /**
     * 查看用户详情
     */
    @GetMapping("/{userId}")
    public Result<UserDetailsVO> getInfo(@PathVariable("userId") Long userId) {
        if (userId == null) return Result.error("用户ID不能为空");
        return Result.success(sysUserService.getUserDetails(userId));
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody UserDetailsVO user) {
        sysUserService.addUser(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> edit(@RequestBody UserDetailsVO user) {
        sysUserService.updateUser(user);
        return Result.success();
    }
//    @DeleteMapping
//    public Result<Void> delete(@RequestBody List<Long> ids) {
//        if (ids == null || ids.isEmpty()) {
//            return Result.error("ids不能为空");
//        }
//        sysUserService.batchDelete(ids);
//        return Result.success();
//    }

}