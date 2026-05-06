# 📋 Day 9 任务清单：后端代码质量提升

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | 全局异常处理完善 | 🔴 高 | 补充 6 种异常处理，@Slf4j 替换 printStackTrace |
| 2 | Controller 参数校验 | 🔴 高 | DTO 替换 Map，@Valid 注解校验 |
| 3 | CORS 配置收紧 | 🟡 中 | 从配置文件读取允许的域名 |
| 4 | common-log 模块整理 | 🟡 中 | 日志枚举移到 common-log，理清依赖方向 |

## 当前已有基础

- `GlobalExceptionHandler`：只处理 ServiceException 和兜底 Exception，用 e.printStackTrace()
- `ServiceException`：已有 code 字段，但 GlobalExceptionHandler 未使用
- `WxInteractionController`：用 Map<String, Object> 接收参数，无类型安全
- `AdminNoteController`：audit 方法用 Map<String, Integer> 接收状态
- `CorsConfig`：addAllowedOriginPattern("*")，过于宽松
- `LogModule`、`LogAction`、`LogStatus`：在 common 模块的 enums 包下
- `common-log` 模块：依赖 common 模块，但 LogAspect 使用 common.enums.LogAction

## 需要优化的问题清单

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | 异常处理不完整 | 只有 2 种异常处理 | 补充 5 种常见异常 |
| 2 | e.printStackTrace() | 直接打印到控制台 | 用 @Slf4j log.error() |
| 3 | ServiceException code 未使用 | GlobalExceptionHandler 返回 500 | 根据 code 返回对应状态码 |
| 4 | Map 接收参数 | 无类型安全，无校验 | DTO + @Valid |
| 5 | CORS 过于宽松 | 允许所有域名 | 配置文件控制 |
| 6 | 日志枚举位置 | 在 common 模块 | 移到 common-log 模块 |

---

# 📝 Day 9 提示词

## 提示词 1：全局异常处理完善

```
完善 GlobalExceptionHandler，补充 6 种常见异常处理，使用 @Slf4j。

⚠️ 前置依赖：需要添加 spring-boot-starter-validation

=== 0. 添加 validation 依赖 ===

文件：common/pom.xml

在 <dependencies> 中添加：
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

说明：@Valid 注解和 MethodArgumentNotValidException 需要 Hibernate Validator 实现，spring-boot-starter-web 不包含。

=== 1. 修改 GlobalExceptionHandler ===

文件：common/src/main/java/com/littlewin/common/exception/GlobalExceptionHandler.java

完整替换为：

package com.littlewin.common.exception;

import com.littlewin.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（自定义）
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.build(e.getCode(), e.getMessage(), null);
    }

    /**
     * @Valid 校验失败（请求体参数校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.build(400, message, null);
    }

    /**
     * 路径参数/查询参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("约束违反: {}", message);
        return Result.build(400, message, null);
    }

    /**
     * 权限不足（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.build(403, "权限不足", null);
    }

    /**
     * 未认证（401）
     * 注意：Spring Security 的 JWT 过滤器链有自己的 401 处理逻辑（JwtAuthenticationFilter 中 clearContext 后由框架返回 401），
     * 此处理器作为额外保障，处理非标准流程中的认证异常。
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthentication(AuthenticationException e) {
        log.warn("未认证: {}", e.getMessage());
        return Result.build(401, "未登录或Token已过期", null);
    }

    /**
     * 请求体解析失败（JSON 格式错误、类型不匹配）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.build(400, "请求参数格式错误", null);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.build(500, "系统异常", null);
    }
}

改动说明：
1. 添加 @Slf4j 注解
2. ServiceException：使用 Result.build(e.getCode(), ...) 返回业务状态码
3. 新增 MethodArgumentNotValidException：@Valid 校验失败返回 400
4. 新增 ConstraintViolationException：路径参数校验失败返回 400
5. 新增 AccessDeniedException：权限不足返回 403
6. 新增 AuthenticationException：未认证返回 401
7. 新增 HttpMessageNotReadableException：JSON 解析失败返回 400
8. 兜底 Exception：用 log.error("系统异常", e) 替换 e.printStackTrace()

验证：
1. 未携带 Token 访问受保护接口 → 返回 401 "未登录或Token已过期"
2. 用普通用户访问管理员接口 → 返回 403 "权限不足"
3. 提交不合法参数 → 返回 400（需配合提示词 2 的 @Valid）
```

---

## 提示词 2：Controller 参数校验（DTO 替换 Map）

```
将 Controller 中的 Map 参数替换为 DTO，添加 @Valid 校验。

=== 1. 创建 InteractionToggleDTO ===

新建文件：note/src/main/java/com/littlewin/note/domain/dto/InteractionToggleDTO.java

package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InteractionToggleDTO {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    @NotBlank(message = "互动类型不能为空")
    private String type; // "like" 或 "collect"
}

=== 2. 创建 InteractionBatchDTO ===

新建文件：note/src/main/java/com/littlewin/note/domain/dto/InteractionBatchDTO.java

package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InteractionBatchDTO {
    @NotEmpty(message = "笔记ID列表不能为空")
    private List<Long> noteIds;
}

=== 3. 创建 NoteAuditDTO ===

新建文件：note/src/main/java/com/littlewin/note/domain/dto/NoteAuditDTO.java

package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteAuditDTO {
    @NotNull(message = "审核状态不能为空")
    private Integer status; // 1 上架，3 下架
}

注意：status 用 Integer 类型，DTO 只校验非空。值域校验（只能是 1 或 3）放在 Service 层。

=== 4. 修改 WxInteractionController ===

文件：note/src/main/java/com/littlewin/note/controller/WxInteractionController.java

修改 toggle 方法：
- 参数从 @RequestBody Map<String, Object> params 改为 @RequestBody @Valid InteractionToggleDTO dto
- 参数获取从 params.get("noteId") 改为 dto.getNoteId()

修改 batchGetStatus 方法：
- 参数从 @RequestBody Map<String, List<Long>> params 改为 @RequestBody @Valid InteractionBatchDTO dto
- 参数获取从 params.get("noteIds") 改为 dto.getNoteIds()

=== 5. 修改 AdminNoteController ===

文件：note/src/main/java/com/littlewin/note/controller/AdminNoteController.java

修改 audit 方法：
- 参数从 @RequestBody Map<String, Integer> body 改为 @RequestBody @Valid NoteAuditDTO dto
- 参数获取从 body.get("status") 改为 dto.getStatus()

=== 5.1 AdminNoteService 添加值域校验 ===

文件：note/src/main/java/com/littlewin/note/service/impl/AdminNoteServiceImpl.java

在 auditNote 方法中添加值域校验：
public void auditNote(Long id, Integer status) {
    if (status != 1 && status != 3) {
        throw new ServiceException("状态只能是 1（上架）或 3（下架）");
    }
    // ... 原有逻辑
}

=== 5.2 DTO 替换 Map 的附加收益 ===

原来 Map 接收参数时，Long noteId = Long.valueOf(params.get("noteId").toString()) 如果前端传非数字会抛 NumberFormatException。
改为 DTO 后，Spring 自动反序列化，类型不匹配会触发 MethodArgumentNotValidException，由 GlobalExceptionHandler 返回 400，体验更好。

=== 6. AdminAuthController.login 不改造（说明） ===

AdminAuthController.login 方法用 @RequestBody Map<String, String> 接收 username/password，但本次不改造，原因：
1. 登录接口参数简单（只有 2 个字段），Map 足够
2. 登录接口的校验逻辑在 Service 层（验证用户名密码），不在 Controller 层
3. 改造收益低，不影响代码质量

验证：
1. POST /api/wx/interactions 不传 noteId → 返回 400 "笔记ID不能为空"
2. POST /api/wx/interactions/status 不传 noteIds → 返回 400 "笔记ID列表不能为空"
3. PUT /api/admin/notes/1/audit 不传 status → 返回 400 "审核状态不能为空"
4. PUT /api/admin/notes/1/audit 传 status=999 → 返回 400 "状态只能是 1（上架）或 3（下架）"（Service 层校验）
```

---

## 提示词 3：CORS 配置收紧

```
CORS 从允许所有域名改为从配置文件读取。

=== 1. application-dev.yml 添加 CORS 配置 ===

文件：admin/src/main/resources/application-dev.yml

添加：
# -------------------------------
# CORS 跨域配置
# -------------------------------
cors:
  allowed-origins:
    - http://localhost:8848

=== 2. 修改 CorsConfig ===

文件：framework/src/main/java/com/littlewin/framework/config/CorsConfig.java

完整替换为：

package com.littlewin.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:8848}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 从配置文件读取允许的域名
        for (String origin : allowedOrigins) {
            config.addAllowedOriginPattern(origin);
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

=== 3. 生产环境 CORS 配置（参考） ===

生产环境应配置实际域名，而非 localhost：

文件：admin/src/main/resources/application-prod.yml（如有）

cors:
  allowed-origins:
    - https://your-domain.com
    - https://admin.your-domain.com

验证：
1. 访问 http://localhost:8848 的请求 → 正常跨域
2. 访问其他域名的请求 → 被 CORS 拦截
```

---

## 提示词 4：common-log 模块整理

```
将日志相关枚举从 common 模块移到 common-log 模块，理清依赖方向。

⚠️ 当前状态：
- LogModule、LogAction、LogStatus 在 common/src/main/java/com/littlewin/common/enums/
- common-log 模块依赖 common 模块
- LogAspect（在 common-log）使用 LogAction（在 common）
- 业务 Controller 使用 LogModule、LogAction（在 common）

⚠️ 问题：
- 日志相关枚举应该属于日志模块，而不是通用模块
- 但业务 Controller 需要引用这些枚举来标注 @Log 注解

⚠️ 方案分析：
方案 A：枚举移到 common-log → 业务模块需要依赖 common-log（已有依赖，可行）
方案 B：枚举留在 common → 保持现状（简单，但语义不清晰）

考虑到：
1. system、note 模块已经依赖 common-log（通过 @Log 注解使用）
2. 移动枚举不会破坏现有依赖关系
3. 语义更清晰：日志相关的东西都在 common-log

=== 0. 显式添加 common-log 依赖 ===

虽然 note 和 system 模块通过 framework 传递依赖了 common-log，但最佳实践是显式声明直接依赖。

文件：note/pom.xml

在 <dependencies> 中添加：
<dependency>
    <groupId>com.littlewin</groupId>
    <artifactId>common-log</artifactId>
</dependency>

文件：system/pom.xml

在 <dependencies> 中添加：
<dependency>
    <groupId>com.littlewin</groupId>
    <artifactId>common-log</artifactId>
</dependency>

=== 1. 移动枚举文件 ===

将以下 3 个文件从 common 模块移动到 common-log 模块：

源文件：
- common/src/main/java/com/littlewin/common/enums/LogModule.java
- common/src/main/java/com/littlewin/common/enums/LogAction.java
- common/src/main/java/com/littlewin/common/enums/LogStatus.java

目标目录：
- common-log/src/main/java/com/littlewin/common/log/enums/

修改 package 声明：
- package com.littlewin.common.enums;
+ package com.littlewin.common.log.enums;

=== 2. 修改 Log.java 注解 ===

文件：common-log/src/main/java/com/littlewin/common/log/annotation/Log.java

修改 import：
- import com.littlewin.common.enums.LogAction;
+ import com.littlewin.common.log.enums.LogAction;

- import com.littlewin.common.enums.LogModule;
+ import com.littlewin.common.log.enums.LogModule;

完整代码：
package com.littlewin.common.log.annotation;

import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    LogModule module();
    LogAction action();
    String desc() default "";
}

=== 3. 更新所有引用 ===

全局搜索替换：
- import com.littlewin.common.enums.LogModule;
+ import com.littlewin.common.log.enums.LogModule;

- import com.littlewin.common.enums.LogAction;
+ import com.littlewin.common.log.enums.LogAction;

注意：LogStatus 已选择删除方案，无需替换。

涉及文件（通过搜索确认）：
- common-log/src/main/java/com/littlewin/common/log/aspect/LogAspect.java
- 所有使用 @Log 注解的 Controller（WxNoteController、WxInteractionController、AdminNoteController 等）
- 所有使用 LogContext 的 Service

=== 4. 处理 LogStatus 枚举 ===

LogStatus 枚举当前没有被任何代码引用（LogAspect 中直接用硬编码 1/0），有两个选择：

方案 A（推荐）：直接删除 LogStatus.java，减少维护成本
- 删除 common/src/main/java/com/littlewin/common/enums/LogStatus.java
- LogAspect 中的 log.setStatus(1) / log.setStatus(0) 保持不变

方案 B：移动并在 LogAspect 中改用
- 移动到 common-log/src/main/java/com/littlewin/common/log/enums/LogStatus.java
- 修改 LogAspect：log.setStatus(LogStatus.SUCCESS.getCode())

本提示词采用方案 A，直接删除 LogStatus.java。

=== 5. 删除 common 模块中的旧文件 ===

确认所有引用已更新后，删除：
- common/src/main/java/com/littlewin/common/enums/LogModule.java
- common/src/main/java/com/littlewin/common/enums/LogAction.java
- common/src/main/java/com/littlewin/common/enums/LogStatus.java（已无引用，直接删除）

=== 6. 验证依赖方向 ===

整理后的依赖关系：
- common（基础模块，无依赖）
- common-log（日志模块，依赖 common）
- framework（框架模块，依赖 common）
- system（系统模块，依赖 common、common-log、framework）
- note（笔记模块，依赖 common、common-log、framework）

验证：
1. Maven 编译通过：mvn clean compile
2. 所有 @Log 注解正常使用
3. 操作日志记录功能正常
```

---

# ⏱️ Day 9 执行顺序

| 顺序 | 提示词 | 前置依赖 | 预计耗时 |
|:--:|:---|:--:|:--:|
| 1️⃣ | 提示词 1：全局异常处理完善 | 无 | 20 分钟 |
| 2️⃣ | 提示词 2：Controller 参数校验 | 无 | 30 分钟 |
| 3️⃣ | 提示词 3：CORS 配置收紧 | 无 | 10 分钟 |
| 4️⃣ | 提示词 4：common-log 模块整理 | 无 | 20 分钟 |

> 4 个提示词互相独立，可以按任意顺序执行。提示词 4 涉及文件移动，建议最后执行。

---

# 🔍 Day 9 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| GlobalExceptionHandler.java | 重写 | 补充 5 种异常处理 |
| InteractionToggleDTO.java | 新建 | 点赞/收藏切换 DTO |
| InteractionBatchDTO.java | 新建 | 批量查询状态 DTO |
| NoteAuditDTO.java | 新建 | 笔记审核 DTO |
| WxInteractionController.java | 修改 | Map → DTO + @Valid |
| AdminNoteController.java | 修改 | Map → DTO + @Valid |
| CorsConfig.java | 修改 | 配置文件读取域名 |
| application-dev.yml | 修改 | 添加 CORS 配置 |
| LogModule.java | 移动 | common → common-log |
| LogAction.java | 移动 | common → common-log |
| LogStatus.java | 删除 | 无引用，直接删除 |
| 所有 @Log 使用处 | 修改 | 更新 import 路径 |

---

# ⚠️ 风险提示

| 风险 | 影响 | 应对 |
|:---:|:---|:---|
| DTO 替换 Map 后前端参数名不匹配 | 接口报错 | 确认前端传参字段名与 DTO 属性名一致 |
| 枚举移动后 import 未更新 | 编译失败 | 全局搜索替换，确认无遗漏 |
| CORS 收紧后开发环境跨域失败 | 前端无法请求 | 确认 localhost:5173 在白名单中 |
