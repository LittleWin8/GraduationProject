// 请求响应参数（不包含data）
export interface Result {
  code: string;
  msg: string;
}

// 请求响应参数（包含data）
export interface ResultData<T = any> extends Result {
  data: T;
}

// 分页响应参数
export interface ResPage<T> {
  list: T[];
  pageNum: number;
  pageSize: number;
  total: number;
}

// 分页请求参数
export interface ReqPage {
  pageNum: number;
  pageSize: number;
}

// 文件上传模块
export namespace Upload {
  export interface ResFileUrl {
    fileUrl: string;
  }
}

// 登录模块
export namespace Login {
  export interface ReqLoginForm {
    username: string;
    password: string;
  }
  export interface ResLogin {
    token: string;
  }
  // 适配后端的 getUserInfo 接口返回的数据结构
  export interface ResUserInfo {
    userId: number;
    name: string;
    account: string; // 对应后端的 identifier
    avatar: string;
    roles: string[]; // 对应后端的 role_key 数组
  }
  export interface ResAuthButtons {
    [key: string]: string[];
  }
}

// 用户管理模块
export namespace User {
  // 请求参数：对应后端的 UserQueryDTO
  export interface ReqUserParams extends ReqPage {
    authType?: string; // 认证类型：password / wx_openid
    identifier?: string; // 用户名（模糊查询）
    gender?: number; // 性别
    city?: string; // 城市
    phone?: string;
    roleId?: number[]; // 角色ID
    status?: number;
    startTime?: string; // 创建时间-开始
    endTime?: string; // 创建时间-结束
    /** 排序字段 (对应数据库列名，如 u.user_id) */
    orderColumn?: string;
    /** 排序规则 (ascending / descending) */
    orderRule?: string;
  }

  export interface UserListVO {
    userId: number;
    nickname: string;
    authType: string;
    identifier: string;
    gender: number;
    phone: string;
    city: string;
    createTime: string; // "yyyy-MM-dd HH:mm:ss"
  }

  export interface UserDetailsVO {
    // 1. 基础信息
    userId: number;
    nickname: string;
    avatar: string;
    status: number;

    // 2. 认证信息
    identifier: string;
    authType: string;

    // 3. 详细资料
    gender: number;
    phone: string;
    email: string;
    birthday: string; // 对应 LocalDate → "yyyy-MM-dd"
    city: string;
    signature: string;

    // 4. 角色信息
    roleIds: number[];
    roleNames: string[];
  }

  export interface UserUpdateDTO {
    userId: number;

    // sys_user
    nickname: string;
    avatar: string;
    status: number;

    // user_info
    gender: number;
    phone: string;
    email: string;
    city: string;
    signature: string;

    // 角色
    roleIds: number[];
  }
}

// 字典模块
export namespace Dict {
  // 字典类型请求参数
  export interface ReqDictTypeParams extends ReqPage {
    dictName?: string;
    status?: number;
  }
  // 字典类型响应对象
  export interface ResDictType {
    dictId: number;
    dictName: string;
    dictType: string;
    status: number;
    remark: string;
    createTime: string;
  }
  // 字典数据请求参数
  export interface ReqDictDataParams extends ReqPage {
    dictType: string; // 必传：关联的字典类型
    dictLabel?: string; // 搜索：标签
    status?: number; // 搜索：状态
  }
  // 字典数据响应对象
  export interface ResDictData {
    dataId: number;
    dictType: string;
    dictLabel: string;
    dictValue: string;
    tagType: string;
    sortOrder: number;
    status: number;
    remark: string;
    createTime: string;
  }
}

// 角色管理模块
export namespace Role {
  // ==================== 角色模块类型定义 ====================

  /** 角色实体 */
  export interface SysRole {
    roleId: number;
    roleName: string;
    roleKey: string;
    sortOrder: number;
    status: number;
    createTime: string;
  }

  /** 菜单实体 */
  export interface SysMenu {
    menuId: number;
    parentId: number;
    name: string;
    path: string;
    component: string;
    redirect?: string;
    menuType: "M" | "C" | "F"; // M目录 C菜单 F按钮
    title: string;
    icon?: string;
    isLink?: string;
    isHide: number;
    isFull: number;
    isAffix: number;
    isKeepAlive: number;
    activeMenu?: string;
    perms?: string;
    sortOrder: number;
    createTime: string;
  }

  /** 菜单树节点 */
  export interface MenuTreeVO {
    menu: SysMenu;
    children: MenuTreeVO[];
  }

  /** RoleDTO（新增/编辑） */
  export interface RoleDTO {
    roleId?: number;
    roleName: string;
    roleKey: string;
    sortOrder?: number;
    status?: number;
    menuIds?: number[];
  }

  /** 角色菜单分配参数 */
  export interface RoleMenuDTO {
    roleId: number;
    menuIds: number[];
  }

  /** 角色查询参数 */
  export interface ReqRoleParams extends ReqPage {
    roleName?: string;
    roleKey?: string;
    status?: number;
  }

  /** 角色列表响应 */
  export interface ResRoleList {
    list: SysRole[];
    total: number;
  }
}
