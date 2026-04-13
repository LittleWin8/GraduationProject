package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // 使用你项目中的 Result 工具类包装一下
        return Result.success(sysUserService.getUserPageList(queryDTO));
    }
}