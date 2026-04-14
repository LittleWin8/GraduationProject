# 系统后端API接口设计文档

## 一、统一响应格式

统一响应格式参见：[Result.java](../../../smart-note-system/common/src/main/java/com/littlewin/common/core/Result.java)

## 二、Web 管理端接口（/api/admin）

------

### 1. 认证模块（/api/admin/auth）

#### 1.1 管理员登录

> **请求路径**：`/api/admin/auth/login`
> **请求方式**：POST
> **接口描述**：管理员通过账号密码登录系统并获取身份令牌

**请求参数：**

| 参数名   | 类型   | 是否必须 | 备注       |
| :------- | :----- | :------- | :--------- |
| username | string | 必须     | 管理员账号 |
| password | string | 必须     | 管理员密码 |

**请求样例：**

```json
{
    "username": "admin",
    "password": "123456"
}
```

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "token": "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc3NTcwNDI..."
    },
    "timestamp": 1775617892558
}
```

------

#### 1.2 管理员退出登录

> **请求路径**：`/api/admin/auth/logout`
> **请求方式**：POST
> **接口描述**：管理员退出登录

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": "退出成功",
    "timestamp": 1775719023180
}
```

------

#### 1.3 获取菜单权限列表

> **请求路径**：`/api/admin/auth/getAuthMenuList`
> **请求方式**：GET
> **接口描述**：获取当前用户的路由菜单（树形结构），适配 Geeker-Admin

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "path": "/home/index",
            "name": "home",
            "component": "/home/index",
            "redirect": null,
            "meta": {
                "title": "首页",
                "icon": "HomeFilled",
                "isLink": "",
                "isHide": false,
                "isFull": false,
                "isAffix": false,
                "isKeepAlive": true,
                "activeMenu": null
            },
            "children": []
        }
    ],
    "timestamp": 1775896878185
}
```



------

#### 1.4 获取按钮权限列表

> **请求路径**：`/api/admin/auth/getAuthButtonList`
> **请求方式**：GET
> **接口描述**：获取当前用户的按钮权限标识集合

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "authButton": ["add", "edit", "delete", "export", "batchDelete"],
        "useProTable": ["add", "export", "batchDelete"]
    },
    "timestamp": 1775896878185
}
```



------

#### 1.5 获取当前用户信息

> **请求路径**：`/api/admin/auth/getUserInfo`
> **请求方式**：GET
> **接口描述**：获取当前登录用户的详细信息

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "userId": 1,
        "name": "超级管理员",
        "avatar": "",
        "account": "admin",
        "gender": 1,
        "phone": "18888888888",
        "email": "admin@example.com",
        "birthday": null,
        "city": "北京",
        "signature": "系统最高管理员，专注架构设计",
        "lastLoginIp": "127.0.0.1",
        "lastLoginTime": "2026-04-07 10:30:00",
        "roles": ["admin"]
    },
    "timestamp": 1775896878185
}
```

------

### 2. 账户管理模块（/api/admin/sys/user）

#### 2.1 获取用户分页列表

> **请求路径**：`/api/admin/sys/user/list`
> **请求方式**：GET
> **接口描述**：分页查询用户列表，支持多条件筛选

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**请求参数：**

| 参数名     | 类型   | 是否必须 | 备注                               |
| :--------- | :----- | :------- | :--------------------------------- |
| pageNum    | int    | 否       | 当前页码，默认 1                   |
| pageSize   | int    | 否       | 每页条数，默认 10                  |
| authType   | string | 否       | 认证类型：password / wx_openid     |
| identifier | string | 否       | 用户名/标识，支持模糊查询          |
| gender     | int    | 否       | 性别：0 未知，1 男，2 女           |
| city       | string | 否       | 城市                               |
| roleName   | string | 否       | 角色名称                           |
| status     | int    | 否       | 状态：1 正常，0 禁用，2 注销       |
| startTime  | string | 否       | 注册开始时间 (yyyy-MM-dd HH:mm:ss) |
| endTime    | string | 否       | 注册结束时间 (yyyy-MM-dd HH:mm:ss) |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "records": [
            {
                "userId": 1,
                "nickname": "超级管理员",
                "authType": "password",
                "identifier": "admin",
                "gender": 1,
                "phone": "18888888888",
                "city": "北京",
                "createTime": "2026-01-01 00:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    },
    "timestamp": 1775896878185
}
```

------

### 3. 字典管理模块（/api/admin/sys/dict）

#### 3.1 根据字典类型查询数据

> **请求路径**：`/api/admin/sys/dict/type/{dictType}`
> **请求方式**：GET
> **接口描述**：根据字典类型获取字典数据列表（仅返回正常状态）

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**路径参数：**

| 参数名   | 类型   | 是否必须 | 备注                                   |
| :------- | :----- | :------- | :------------------------------------- |
| dictType | string | 必须     | 字典类型，如：user_status、note_status |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "dataId": 1,
            "dictType": "user_status",
            "dictLabel": "正常",
            "dictValue": "1",
            "tagType": "success",
            "sortOrder": 1,
            "status": 1
        },
        {
            "dataId": 2,
            "dictType": "user_status",
            "dictLabel": "禁用",
            "dictValue": "0",
            "tagType": "danger",
            "sortOrder": 2,
            "status": 1
        }
    ],
    "timestamp": 1775896878185
}
```



------

#### 3.2 查询所有字典类型

> **请求路径**：`/api/admin/sys/dict/type/list`
> **请求方式**：GET
> **接口描述**：获取所有字典类型列表（用于字典管理页面）

**请求头：**

| 参数名        | 类型   | 是否必须 | 备注                  |
| :------------ | :----- | :------- | :-------------------- |
| Authorization | string | 必须     | Bearer + 空格 + Token |

**响应样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "dictId": 1,
            "dictName": "用户性别",
            "dictType": "user_gender",
            "status": 1,
            "remark": "user_info.gender"
        },
        {
            "dictId": 2,
            "dictName": "用户状态",
            "dictType": "user_status",
            "status": 1,
            "remark": "sys_user.status"
        }
    ],
    "timestamp": 1775896878185
}
```

## 三、微信小程序接口（/api/app）

>  *待开发*



## 四、接口汇总表

| 模块     | 接口             | 路径                                  | 方式 |
| :------- | :--------------- | :------------------------------------ | :--- |
| 认证     | 登录             | `/api/admin/auth/login`               | POST |
| 认证     | 退出             | `/api/admin/auth/logout`              | POST |
| 认证     | 获取菜单列表     | `/api/admin/auth/getAuthMenuList`     | GET  |
| 认证     | 获取按钮权限     | `/api/admin/auth/getAuthButtonList`   | GET  |
| 认证     | 获取用户信息     | `/api/admin/auth/getUserInfo`         | GET  |
| 账户管理 | 用户分页列表     | `/api/admin/sys/user/list`            | GET  |
| 字典管理 | 按类型查字典数据 | `/api/admin/sys/dict/type/{dictType}` | GET  |
| 字典管理 | 查所有字典类型   | `/api/admin/sys/dict/type/list`       | GET  |
