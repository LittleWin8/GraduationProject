package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.dto.UserUpdateDTO;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    @PutMapping("/edit")
    public Result<Void> edit(@RequestBody UserDetailsVO user) {
        sysUserService.updateUser(user);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody UserUpdateDTO updateDTO) {
        sysUserService.resetPassword(updateDTO);
        return Result.success("密码重置成功");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestBody List<Long> ids) {
        sysUserService.batchDeleteUsers(ids);
        return Result.success("用户删除成功");
    }

}