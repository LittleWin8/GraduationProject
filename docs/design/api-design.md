# 系统后端API接口设计文档

## 一、统一响应格式

```java
package com.littlewin.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;
    private Long timestamp;


    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 自定义返回
     */
    public static <T> Result<T> build(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }


}
```



## 二、Web 管理端接口（/api/admin）

------

### 1. 管理员登录

#### 1.1 基本信息

> 请求路径：`/api/admin/auth/login`
>
> 请求方式：POST
>
> 接口描述：管理员通过账号密码登录系统并获取身份令牌（Token）

#### 1.2 请求参数

| **参数名** |  类型  | 是否必须 |    备注    |
| :--------: | :----: | :------: | :--------: |
|  username  | string |   必须   | 管理员账号 |
|  password  | string |   必须   | 管理员密码 |

**请求数据样例：**

```json
{
    "username": "admin",
    "password": "123456"
}
```



#### 1.3 响应数据

**参数格式：** `application/json`

**参数说明：**

| **参数名** | **类型** | **是否必须** | **备注**                           |
| ---------- | -------- | ------------ | ---------------------------------- |
| code       | number   | 必须         | 响应码，200 代表成功，其他代表失败 |
| msg        | string   | 非必须       | 提示信息                           |
| data       | object   | 非必须       | 返回的数据主体                     |
| \|- token  | string   | 非必须       | JWT 访问令牌 (含 Bearer 前缀)      |
| timestamp  | number   | 必须         | 服务端响应时间戳                   |

**成功响应数据样例：**

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



### 2. 管理员退出登录

#### 2.1 基本信息

> 请求路径：`/api/admin/auth/logout`
>
> 请求方式：POST
>
> 接口描述：管理员退出登录，使当前 Token 失效

#### 2.2.1 请求参数

无

#### 2.2.2 请求头（可选）

| **参数名**    | 类型   | 是否必须 | 备注                     |
| :------------ | :----- | :------- | :----------------------- |
| Authorization | string | 建议     | Bearer + 空格 + 登录令牌 |

#### 2.3 响应数据

**参数格式：** `application/json`

**参数说明：**

| **参数名** | **类型** | **是否必须** | **备注**                           |
| ---------- | -------- | ------------ | ---------------------------------- |
| code       | number   | 必须         | 响应码，200 代表成功，其他代表失败 |
| msg        | string   | 非必须       | 提示信息                           |
| data       | object   | 非必须       | 返回退出成功消息                   |
| timestamp  | number   | 必须         | 服务端响应时间戳                   |

**响应数据样例：**

```json
{
    "code": 200,
    "msg": "success",
    "data": "退出成功",
    "timestamp": 1775719023180
}
```





## 三、微信小程序接口（/api/app）

