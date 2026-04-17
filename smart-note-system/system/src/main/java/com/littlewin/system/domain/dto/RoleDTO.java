package com.littlewin.system.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer sortOrder;
    private Integer status;
    private List<Long> menuIds;  // 分配的菜单权限ID列表
}