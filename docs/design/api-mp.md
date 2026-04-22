# 小程序端 API 文档

## 基本信息

- **基础URL**: `本机IP:8080`
- **请求方式**: HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8

## 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1703123456789
}
```

## 认证方式

除登录接口外，所有接口需要在请求头中携带 token：`Authorization: Bearer {token}`

------

## 一、认证模块 (2个接口)

### 1.1 微信登录

**接口**: `POST /api/wx/auth/login`

**描述**: 微信小程序登录，自动注册或登录

**请求参数**:

| 参数     | 类型   | 必填 | 说明         |
| :------- | :----- | :--- | :----------- |
| code     | string | 是   | 微信登录code |
| nickname | string | 否   | 用户昵称     |
| avatar   | string | 否   | 头像URL      |

**请求示例**:

```json
{
  "code": "wx_code_xxxxx",
  "nickname": "张三",
  "avatar": "https://example.com/avatar.jpg"
}
```

**响应示例**:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "isNewUser": false,
    "token": "eyJhbGciOiJIUzI1NiIs..."
  },
  "timestamp": 1703123456789
}
```

------

### 1.2 退出登录

**接口**: `POST /api/wx/auth/logout`

**描述**: 退出登录，清除服务端session

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```

------


## 二、用户模块 (3个接口)

### 2.1 获取个人信息

**接口**: `GET /api/wx/user`

**描述**: 获取当前用户个人信息

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "userId": 1,
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "gender": 1,
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "birthday": "1990-01-01",
    "city": "北京",
    "signature": "爱学习的程序员",
    "createTime": "2024-01-01 00:00:00"
  },
  "timestamp": 1703123456789
}
```



------

### 2.2 更新个人信息

**接口**: `PUT /api/wx/user`

**描述**: 更新当前用户个人信息

**请求参数**:

| 参数      | 类型   | 必填 | 说明                |
| :-------- | :----- | :--- | :------------------ |
| nickname  | string | 否   | 昵称                |
| avatar    | string | 否   | 头像URL             |
| gender    | int    | 否   | 性别：0未知 1男 2女 |
| phone     | string | 否   | 手机号              |
| email     | string | 否   | 邮箱                |
| birthday  | string | 否   | 生日 YYYY-MM-DD     |
| city      | string | 否   | 所在城市            |
| signature | string | 否   | 个性签名            |

**请求示例**:

json

```
{
  "nickname": "李四",
  "signature": "记录每一天"
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

### 2.3 获取统计数据

**接口**: `GET /api/wx/user/stats`

**描述**: 获取用户统计数据，包含收藏列表

**请求参数**:

| 参数 | 类型 | 必填 | 说明             |
| :--- | :--- | :--- | :--------------- |
| page | int  | 否   | 页码，默认1      |
| size | int  | 否   | 每页数量，默认20 |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "noteCount": 15,
    "likeCount": 32,
    "favoriteCount": 8,
    "favorites": {
      "records": [
        {
          "noteId": 100,
          "title": "Spring Boot 学习笔记",
          "content": "这是笔记摘要...",
          "createTime": "2024-01-15 10:30:00"
        }
      ],
      "total": 8,
      "page": 1,
      "size": 20
    }
  },
  "timestamp": 1703123456789
}
```



------

## 三、笔记模块 (7个接口)

### 3.1 获取笔记列表

**接口**: `GET /api/wx/notes`

**描述**: 获取公开笔记或我的笔记列表

**请求参数**:

| 参数       | 类型   | 必填 | 说明                                         |
| :--------- | :----- | :--- | :------------------------------------------- |
| type       | string | 是   | 类型：`public`(公开/社区) / `my`(我的)       |
| page       | int    | 否   | 页码，默认1                                  |
| size       | int    | 否   | 每页数量，默认20                             |
| categoryId | int    | 否   | 分类ID筛选（仅type=my时有效）                |
| tagId      | int    | 否   | 标签ID筛选（仅type=my时有效）                |
| status     | int    | 否   | 状态：0草稿 1正常 2回收站（仅type=my时有效） |

**请求示例**:

text

```
GET /api/wx/notes?type=public&page=1&size=20
GET /api/wx/notes?type=my&page=1&size=20&status=1
GET /api/wx/notes?type=my&tagId=1    // 获取标签下的笔记
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "noteId": 100,
        "title": "Spring Boot 学习笔记",
        "content": "笔记摘要或内容...",
        "viewCount": 128,
        "likeCount": 15,
        "commentCount": 3,
        "isPublic": 1,
        "createTime": "2024-01-15 10:30:00",
        "user": {
          "userId": 1,
          "nickname": "张三",
          "avatar": "https://..."
        },
        "category": {
          "categoryId": 1,
          "name": "后端开发"
        },
        "tags": [
          {"tagId": 1, "name": "Java"},
          {"tagId": 2, "name": "Spring"}
        ]
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20
  },
  "timestamp": 1703123456789
}
```



------

### 3.2 获取笔记详情

**接口**: `GET /api/wx/notes/{id}`

**描述**: 查看笔记详情，自动增加浏览量

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 笔记ID |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "noteId": 100,
    "title": "Spring Boot 学习笔记",
    "content": "# Markdown内容\n\n这是完整的笔记内容...",
    "viewCount": 129,
    "likeCount": 15,
    "commentCount": 3,
    "isPublic": 1,
    "createTime": "2024-01-15 10:30:00",
    "updateTime": "2024-01-20 14:00:00",
    "user": {
      "userId": 1,
      "nickname": "张三",
      "avatar": "https://..."
    },
    "category": {
      "categoryId": 1,
      "name": "后端开发"
    },
    "tags": [
      {"tagId": 1, "name": "Java"},
      {"tagId": 2, "name": "Spring"}
    ],
    "aiSummary": {
      "summary": "本文介绍了Spring Boot的基本使用方法...",
      "keywords": "Spring Boot,Java,微服务"
    }
  },
  "timestamp": 1703123456789
}
```



------

### 3.3 创建笔记

**接口**: `POST /api/wx/notes`

**描述**: 创建新笔记，支持Markdown

**请求参数**:

| 参数       | 类型   | 必填 | 说明                         |
| :--------- | :----- | :--- | :--------------------------- |
| title      | string | 是   | 笔记标题                     |
| content    | string | 是   | Markdown内容                 |
| categoryId | int    | 否   | 分类ID                       |
| isPublic   | int    | 否   | 是否公开：0私密 1公开，默认1 |
| tagIds     | array  | 否   | 标签ID数组                   |

**请求示例**:

json

```
{
  "title": "Vue3 学习笔记",
  "content": "# 响应式 API\n\n`ref` 和 `reactive` 的区别...",
  "categoryId": 2,
  "isPublic": 1,
  "tagIds": [3, 4]
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "noteId": 101,
    "createTime": "2024-01-25 09:00:00"
  },
  "timestamp": 1703123456789
}
```



------

### 3.4 更新笔记

**接口**: `PUT /api/wx/notes/{id}`

**描述**: 更新已有笔记

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 笔记ID |

**请求参数**:

| 参数       | 类型   | 必填 | 说明         |
| :--------- | :----- | :--- | :----------- |
| title      | string | 否   | 笔记标题     |
| content    | string | 否   | Markdown内容 |
| categoryId | int    | 否   | 分类ID       |
| isPublic   | int    | 否   | 是否公开     |
| tagIds     | array  | 否   | 标签ID数组   |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

### 3.5 删除笔记

**接口**: `DELETE /api/wx/notes/{id}`

**描述**: 删除笔记（移入回收站或永久删除）

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 笔记ID |

**请求参数**:

| 参数      | 类型    | 必填 | 说明                    |
| :-------- | :------ | :--- | :---------------------- |
| permanent | boolean | 否   | 是否永久删除，默认false |

**请求示例**:

text

```
DELETE /api/wx/notes/101?permanent=false   // 移入回收站
DELETE /api/wx/notes/101?permanent=true    // 永久删除
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

### 3.6 恢复笔记

**接口**: `PUT /api/wx/notes/{id}/restore`

**描述**: 从回收站恢复笔记

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 笔记ID |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

### 3.7 AI生成摘要

**接口**: `POST /api/wx/notes/ai/summary`

**描述**: 调用AI生成笔记摘要和关键词

**请求参数**:

| 参数    | 类型   | 必填 | 说明                 |
| :------ | :----- | :--- | :------------------- |
| content | string | 是   | 笔记内容（Markdown） |

**请求示例**:

json

```
{
  "content": "Spring Boot 是基于 Spring 框架的快速开发框架..."
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "summary": "本文介绍了Spring Boot作为快速开发框架的核心特性...",
    "keywords": "Spring Boot,快速开发,微服务,自动配置"
  },
  "timestamp": 1703123456789
}
```



------

## 四、分类模块 (1个接口)

### 4.1 获取分类列表

**接口**: `GET /api/wx/categories`

**描述**: 获取系统预设分类列表（已启用）

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "categoryId": 1,
      "name": "后端开发",
      "sortOrder": 1
    },
    {
      "categoryId": 2,
      "name": "前端开发",
      "sortOrder": 2
    },
    {
      "categoryId": 3,
      "name": "人工智能",
      "sortOrder": 3
    }
  ],
  "timestamp": 1703123456789
}
```



------

## 五、标签模块 (3个接口)

### 5.1 获取我的标签列表

**接口**: `GET /api/wx/tags`

**描述**: 获取当前用户创建的所有标签

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "tagId": 1,
      "name": "Java",
      "noteCount": 5
    },
    {
      "tagId": 2,
      "name": "Python",
      "noteCount": 3
    }
  ],
  "timestamp": 1703123456789
}
```



------

### 5.2 创建标签

**接口**: `POST /api/wx/tags`

**描述**: 创建自定义标签

**请求参数**:

| 参数 | 类型   | 必填 | 说明     |
| :--- | :----- | :--- | :------- |
| name | string | 是   | 标签名称 |

**请求示例**:

json

```
{
  "name": "Redis"
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "tagId": 3,
    "name": "Redis"
  },
  "timestamp": 1703123456789
}
```



------

### 5.3 删除标签

**接口**: `DELETE /api/wx/tags/{id}`

**描述**: 删除标签（不影响已关联的笔记）

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 标签ID |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



> **说明**: 获取标签下的笔记，请使用 `GET /api/wx/notes?type=my&tagId={id}`，无需单独接口。

------

## 六、互动模块 (2个接口)

### 6.1 点赞/收藏

**接口**: `POST /api/wx/interactions`

**描述**: 点赞/取消点赞 或 收藏/取消收藏

**请求参数**:

| 参数   | 类型   | 必填 | 说明                                 |
| :----- | :----- | :--- | :----------------------------------- |
| noteId | int    | 是   | 笔记ID                               |
| type   | string | 是   | 类型：`like`(点赞) / `collect`(收藏) |

**请求示例**:

json

```
{
  "noteId": 100,
  "type": "like"
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "isLiked": true,
    "likeCount": 16
  },
  "timestamp": 1703123456789
}
```



------

### 6.2 获取互动状态

**接口**: `GET /api/wx/interactions/status/{noteId}` 或 `POST /api/wx/interactions/status`

**描述**: 获取单个或批量笔记的互动状态

**单条查询（GET）**:

text

```
GET /api/wx/interactions/status/100
```



**响应示例（单条）**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "isLiked": true,
    "isCollected": false,
    "likeCount": 16,
    "collectCount": 5
  },
  "timestamp": 1703123456789
}
```



**批量查询（POST）**:

text

```
POST /api/wx/interactions/status
```



**请求参数**:

json

```
{
  "noteIds": [100, 101, 102]
}
```



**响应示例（批量）**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "100": {"isLiked": true, "isCollected": false},
    "101": {"isLiked": false, "isCollected": true},
    "102": {"isLiked": false, "isCollected": false}
  },
  "timestamp": 1703123456789
}
```



------

## 七、评论模块 (3个接口)

### 7.1 获取评论列表

**接口**: `GET /api/wx/comments`

**描述**: 获取笔记的评论列表

**请求参数**:

| 参数   | 类型 | 必填 | 说明             |
| :----- | :--- | :--- | :--------------- |
| noteId | int  | 是   | 笔记ID           |
| page   | int  | 否   | 页码，默认1      |
| size   | int  | 否   | 每页数量，默认20 |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "commentId": 1,
        "content": "写得真好！",
        "createTime": "2024-01-15 11:00:00",
        "user": {
          "userId": 2,
          "nickname": "李四",
          "avatar": "https://..."
        },
        "replyTo": null,
        "replies": []
      }
    ],
    "total": 5,
    "page": 1,
    "size": 20
  },
  "timestamp": 1703123456789
}
```



------

### 7.2 发表评论

**接口**: `POST /api/wx/comments`

**描述**: 对笔记发表评论或回复

**请求参数**:

| 参数     | 类型   | 必填 | 说明                   |
| :------- | :----- | :--- | :--------------------- |
| noteId   | int    | 是   | 笔记ID                 |
| content  | string | 是   | 评论内容               |
| parentId | int    | 否   | 父评论ID（回复时使用） |

**请求示例**:

json

```
{
  "noteId": 100,
  "content": "学习了，感谢分享！"
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "commentId": 6,
    "createTime": "2024-01-25 10:00:00"
  },
  "timestamp": 1703123456789
}
```



------

### 7.3 删除评论

**接口**: `DELETE /api/wx/comments/{id}`

**描述**: 删除自己的评论

**路径参数**:

| 参数 | 类型 | 说明   |
| :--- | :--- | :----- |
| id   | int  | 评论ID |

**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

## 八、日志模块 (1个接口)

### 8.1 上报行为日志

**接口**: `POST /api/wx/log/behavior`

**描述**: 上报用户浏览或搜索行为

**请求参数**:

| 参数    | 类型   | 必填 | 说明                                |
| :------ | :----- | :--- | :---------------------------------- |
| type    | string | 是   | 类型：`view`(浏览) / `search`(搜索) |
| content | string | 是   | 笔记ID 或 搜索关键词                |

**请求示例**:

json

```
{
  "type": "view",
  "content": "100"
}
```



**响应示例**:

json

```
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1703123456789
}
```



------

## 响应码码说明

| code | 说明                    |
| :--- | :---------------------- |
| 200  | 成功                    |
| 401  | 未授权，token无效或过期 |
| 403  | 无权限访问              |
| 404  | 资源不存在              |
| 500  | 服务器内部错误          |

------

## 附录：接口汇总表

| 模块     | 接口数 | 路径前缀                 |
| :------- | :----- | :----------------------- |
| 认证     | 2      | `/api/wx/auth/*`         |
| 用户     | 3      | `/api/wx/user/*`         |
| 笔记     | 7      | `/api/wx/notes/*`        |
| 分类     | 1      | `/api/wx/categories`     |
| 标签     | 3      | `/api/wx/tags/*`         |
| 互动     | 2      | `/api/wx/interactions/*` |
| 评论     | 3      | `/api/wx/comments/*`     |
| 日志     | 1      | `/api/wx/log/*`          |
| **合计** | **22** |                          |

------

## 附录：路径规范说明

所有小程序端接口统一以 `/api/wx/` 开头，与 Web 管理端（`/api/web/`）区分，便于后端进行权限控制和路由隔离。