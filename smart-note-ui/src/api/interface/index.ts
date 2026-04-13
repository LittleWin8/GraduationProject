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
    authType?: string;    // 认证类型：password / wx_openid
    identifier?: string;  // 用户名（模糊查询）
    gender?: number;      // 性别
    city?: string;        // 城市
    roleId?: number;      // 角色ID
    startTime?: string;   // 创建时间-开始
    endTime?: string;     // 创建时间-结束
  }

  // 返回列表项：对应后端的 UserListVO
  export interface ResUserList {
    userId: number;       // 用户ID (BIGINT -> number)
    nickname: string;     // 昵称
    status: number;       // 状态：1正常, 0禁用, 2删除
    authType: string;     // 认证类型
    identifier: string;   // 账号/OpenID
    gender: number;       // 性别
    phone: string;        // 手机号
    city: string;         // 城市
    createTime: string;   // 创建时间
  }

  // 角色信息（用于搜索栏下拉框）
  export interface ResRole {
    roleId: number;
    roleName: string;
    roleKey: string;
  }
  
  export interface ResStatus {
    userLabel: string;
    userValue: number;
  }
  export interface ResGender {
    genderLabel: string;
    genderValue: number;
  }
  export interface ResDepartment {
    id: string;
    name: string;
    children?: ResDepartment[];
  }
  export interface ResRole {
    id: string;
    name: string;
    children?: ResDepartment[];
  }
}
