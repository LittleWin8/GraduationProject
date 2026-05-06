# 📋 Day 7 任务清单：Redis 环境搭建 + JWT 安全优化 + SecurityConfig 修复

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | Docker 部署 Redis 7.2 | 🔴 高 | 编写 docker-compose.yml，验证 Redis 连接 |
| 2 | Spring Boot 集成 Redis | 🔴 高 | 依赖 + 配置 + RedisConfig + RedisService |
| 3 | JWT 安全优化（Bean化 + 黑名单） | 🔴 高 | JwtUtils 重构 + 退出写黑名单 + Filter 校验 |
| 4 | SecurityConfig 放行路径补全 | 🟡 中 | Knife4j 文档路径 + 行为日志 + 微信回调 |

## 当前已有基础

- `JwtUtils`：静态方法 + `@PostConstruct` 赋值静态变量，密钥硬编码 `smart-note-secret-key`
- `JwtAuthenticationFilter`：解析 Token → 加载 UserDetails → 设置 SecurityContext，无黑名单校验
- `SecurityConfig`：只放行了 `/api/admin/auth/login`、`/api/wx/auth/login`、`/api/wx/user/files/**`、`/api/wx/user/avatar`
- `AdminAuthServiceImpl.logout()`：空方法，只记日志，Token 仍可用
- `WxAuthController.logout()`：注释了"后期加 Redis 黑名单"
- `Constants`：只有 `TOKEN_HEADER` 和 `TOKEN_PREFIX`
- 项目无任何 Redis 依赖

## 需要修改的问题清单

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | 无 Redis 环境 | 本地无 Redis | Docker 部署 Redis 7.2，应用可连接 |
| 2 | 无 Redis 依赖 | pom.xml 无 spring-boot-starter-data-redis | 完整集成 Redis（依赖+配置+工具类） |
| 3 | JWT 密钥硬编码 | `jwt.secret: smart-note-secret-key` | 64 字符随机密钥，放 application-dev.yml |
| 4 | JwtUtils 静态方法 | `@PostConstruct` + 静态变量 hack | 改为 Spring Bean 实例方法，依赖注入 |
| 5 | 退出无黑名单 | logout() 空方法 | 退出时 Token 写入 Redis 黑名单 |
| 6 | Filter 无黑名单校验 | 只校验签名和过期 | 增加 Redis 黑名单检查 |
| 7 | Knife4j 文档无法访问 | SecurityConfig 未放行 | 放行 /doc.html 等路径 |
| 8 | 行为日志需登录 | /api/wx/log/behavior 需认证 | 放行该路径 |

---

# 📝 Day 7 提示词

## 提示词 1：Docker 部署 Redis 7.2

```
在 smart-note-system 项目根目录下，创建 Docker Compose 配置文件来部署 Redis 7.2。

需要创建的文件：smart-note-system/docker-compose.yml

文件内容要求：
version: '3.8'
services:
  redis:
    image: redis:7.2-alpine
    container_name: smart-note-redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass smartnote2026 --appendonly yes
    volumes:
      - redis-data:/data
    restart: unless-stopped

volumes:
  redis-data:

验证步骤：
1. 在 smart-note-system 目录下执行 docker-compose up -d
2. 执行 docker exec -it smart-note-redis redis-cli -a smartnote2026 ping
3. 预期返回 PONG

注意事项：
- 密码 smartnote2026 仅用于开发环境
- appendonly yes 开启 AOF 持久化，容器重启数据不丢失
- 如果本地 6379 端口被占用，改为 6380:6379
```

---

## 提示词 2：Spring Boot 集成 Redis（依赖 + 配置 + RedisConfig + RedisService）

```
在 smart-note-system 项目中集成 Spring Data Redis，添加依赖、配置、配置类和工具类。

=== 1. 父 pom.xml 添加依赖管理 ===

文件：smart-note-system/pom.xml

在 <dependencyManagement><dependencies> 中添加：

<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
<!-- Redis 连接池依赖（Lettuce 需要） -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.12.0</version>
</dependency>

=== 2. common 模块 pom.xml 添加 Redis 依赖 ===

文件：smart-note-system/common/pom.xml

在 <dependencies> 中添加：

<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- Redis 连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>

注意：Redis 依赖放在 common 模块，因为 JwtUtils、RedisService 等工具类都在 common 中，且 framework、system、note 模块都依赖 common。

=== 3. application-dev.yml 添加 Redis 配置 ===

文件：smart-note-system/admin/src/main/resources/application-dev.yml（或 application-dev-example.yml）

添加：

# ===============================
# Redis 配置
# ===============================
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: smartnote2026
      database: 0
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: -1ms
      timeout: 3000ms

=== 4. 创建 RedisConfig 配置类 ===

文件：smart-note-system/common/src/main/java/com/littlewin/common/config/RedisConfig.java

package com.littlewin.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

=== 5. 创建 RedisService 工具类 ===

文件：smart-note-system/common/src/main/java/com/littlewin/common/utils/RedisService.java

package com.littlewin.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 通用操作 ====================

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ==================== String 操作 ====================

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * SETNX — 仅当 key 不存在时设置值，返回是否设置成功
     */
    public boolean setNx(String key, Object value, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
    }

    /**
     * 自增 — 返回自增后的值
     */
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自增 + 设置过期时间（仅当 key 首次创建时生效）
     */
    public Long incr(String key, long delta, long timeout, TimeUnit unit) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        if (result != null && result == delta) {
            redisTemplate.expire(key, timeout, unit);
        }
        return result;
    }

    /**
     * 自减
     */
    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }
}

=== 6. 验证 ===

启动 SmartNoteApplication，观察日志中是否出现：
- Lettuce 连接 Redis 成功
- 无 Redis 连接异常

在任意 Controller 中临时注入 RedisService 测试：
@Autowired
private RedisService redisService;

@GetMapping("/test-redis")
public Result<?> testRedis() {
    redisService.set("test:key", "hello-redis", 60, TimeUnit.SECONDS);
    Object val = redisService.get("test:key");
    return Result.success(val);
}

访问该接口，预期返回 "hello-redis"。测试完成后删除此接口。
```

---

## 提示词 3：JWT 安全优化（Bean化 + 密钥加固 + 黑名单）

```
在 smart-note-system 项目中，重构 JWT 体系：JwtUtils 改为 Spring Bean、密钥加固、退出写黑名单、Filter 增加黑名单校验。

=== 1. 修改 application.yml 和 application-dev.yml ===

文件：smart-note-system/admin/src/main/resources/application.yml

将 jwt 配置改为：
jwt:
  secret: ${JWT_SECRET:default-dev-secret-key-please-replace-in-production-env-64chars}
  expire: 86400000

文件：smart-note-system/admin/src/main/resources/application-dev.yml（或 application-dev-example.yml）

添加：
jwt:
  secret: a1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z6a7B8c9D0e1F2

注意：这个 64 字符密钥仅用于开发环境，生产环境通过环境变量 JWT_SECRET 注入。

=== 2. 重构 JwtUtils — 去掉静态方法，改为 Spring Bean ===

文件：smart-note-system/common/src/main/java/com/littlewin/common/utils/JwtUtils.java

完整替换为：

package com.littlewin.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expireTime;

    public String createToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public String getTokenId(String token) {
        return parseToken(token).getId();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getRemainingExpiration(String token) {
        try {
            Date expiration = parseToken(token).getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining > 0 ? remaining : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

关键改动说明：
- 去掉 @PostConstruct + 静态变量 SECRET/EXPIRE_TIME
- 所有方法改为实例方法（非 static）
- 新增 setId(UUID) 为每个 Token 分配唯一 jti（JWT ID），用于黑名单标识
- 新增 getTokenId() 提取 jti
- 新增 getRemainingExpiration() 获取 Token 剩余有效期（黑名单 TTL 用）

=== 3. 修改所有调用 JwtUtils 静态方法的地方 ===

3.1 AdminAuthServiceImpl.java
文件：smart-note-system/system/src/main/java/com/littlewin/system/service/impl/AdminAuthServiceImpl.java

修改：
- 添加注入：@Resource private JwtUtils jwtUtils;
- 将 return JwtUtils.createToken(username); 改为 return jwtUtils.createToken(username);
- 修改 logout() 方法（见下方第 4 步）

3.2 WxUserServiceImpl.java
文件：smart-note-system/system/src/main/java/com/littlewin/system/service/impl/WxUserServiceImpl.java

搜索所有 JwtUtils.createToken 调用，改为注入 jwtUtils 后调用 jwtUtils.createToken()。
当前代码中 login 方法有：return JwtUtils.createToken(openid); 改为 jwtUtils.createToken(openid)

=== 4. 修改退出登录 — 写入 Redis 黑名单 ===

4.1 AdminAuthServiceImpl.logout()
文件：smart-note-system/system/src/main/java/com/littlewin/system/service/impl/AdminAuthServiceImpl.java

添加注入：
@Resource
private RedisService redisService;
@Resource
private JwtUtils jwtUtils;

修改 logout() 方法为：
@Override
@Log(module = LogModule.AUTH, action = LogAction.LOGOUT, desc = "用户退出登录")
public void logout() {
    LoginDTO loginUser = SecurityUtils.getLoginUser();
    if (loginUser != null) {
        LogContext.setBusinessId(loginUser.getUserId());
        LogContext.setDesc("退出登录【" + loginUser.getUsername() + "】");

        // 将当前 Token 写入 Redis 黑名单
        String token = ServletUtils.getTokenFromRequest();
        if (token != null && !token.isEmpty()) {
            try {
                String jti = jwtUtils.getTokenId(token);
                long remaining = jwtUtils.getRemainingExpiration(token);
                if (remaining > 0) {
                    redisService.set("token:blacklist:" + jti, "1", remaining, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                // Token 解析失败（可能已过期），无需加入黑名单
            }
        }
    }
}

4.2 WxAuthController.logout()
文件：smart-note-system/system/src/main/java/com/littlewin/system/controller/WxAuthController.java

添加注入：
@Resource
private RedisService redisService;
@Resource
private JwtUtils jwtUtils;

修改 logout() 方法为：
@PostMapping("/logout")
@Log(module = LogModule.AUTH, action = LogAction.LOGOUT, desc = "微信小程序退出登录")
public Result logout() {
    LoginDTO loginUser = SecurityUtils.getLoginUser();
    if (loginUser != null) {
        LogContext.setBusinessId(loginUser.getUserId());

        String token = ServletUtils.getTokenFromRequest();
        if (token != null && !token.isEmpty()) {
            try {
                String jti = jwtUtils.getTokenId(token);
                long remaining = jwtUtils.getRemainingExpiration(token);
                if (remaining > 0) {
                    redisService.set("token:blacklist:" + jti, "1", remaining, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                // Token 已过期，无需加黑名单
            }
        }
    }
    return Result.success("退出成功");
}

=== 5. ServletUtils 添加获取 Token 的方法 ===

文件：smart-note-system/common/src/main/java/com/littlewin/common/utils/ServletUtils.java

在现有方法后添加：

/**
 * 从当前 HTTP 请求中提取 Bearer Token（去掉 "Bearer " 前缀后的纯 Token 字符串）
 */
public static String getTokenFromRequest() {
    HttpServletRequest request = getRequest();
    if (request == null) return null;
    String header = request.getHeader(Constants.TOKEN_HEADER);
    if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
        return header.substring(Constants.TOKEN_PREFIX.length());
    }
    return null;
}

注意：如果 ServletUtils 中已有 getRequest() 方法则直接用；如果没有，需要添加：
public static HttpServletRequest getRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes == null) return null;
    return ((ServletRequestAttributes) attributes).getRequest();
}

=== 6. 修改 JwtAuthenticationFilter — 增加黑名单校验 ===

文件：smart-note-system/framework/src/main/java/com/littlewin/framework/filter/JwtAuthenticationFilter.java

完整替换为：

package com.littlewin.framework.filter;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(Constants.TOKEN_HEADER);

        if (StringUtils.hasText(header) && header.startsWith(Constants.TOKEN_PREFIX)) {

            String token = header.substring(Constants.TOKEN_PREFIX.length());

            try {
                // 1. 检查 Token 是否在黑名单中（已退出登录）
                String jti = jwtUtils.getTokenId(token);
                if (redisService.hasKey("token:blacklist:" + jti)) {
                    log.debug("Token 已被加入黑名单，拒绝访问: jti={}", jti);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 2. 解析 Token 获取用户标识
                String userId = jwtUtils.getSubject(token);

                // 3. 加载用户详情并设置安全上下文
                if (StringUtils.hasText(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                log.debug("JWT Token 解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

关键改动：
- 注入 JwtUtils（实例方法）替代 JwtUtils（静态方法）
- 注入 RedisService 用于黑名单校验
- Token 解析前先检查黑名单，命中则直接放行（不设置 SecurityContext，后续 authorizeHttpRequests 会返回 401）
- 添加 @Slf4j 替代 e.printStackTrace()
- 增加 SecurityContext 非空判断，避免重复设置

=== 7. 验证流程 ===

1. 启动应用，确保 Redis 连接正常
2. 用 Postman 调用登录接口获取 Token
3. 用该 Token 访问受保护接口 → 预期 200 成功
4. 调用退出登录接口
5. 用同一个 Token 再次访问受保护接口 → 预期 401 拒绝
6. 重新登录获取新 Token → 访问受保护接口 → 预期 200 成功
```

---

## 提示词 4：SecurityConfig 放行路径补全

```
在 smart-note-system 项目中，补全 SecurityConfig 的放行路径，使 Knife4j 文档和部分公开接口可访问。

文件：smart-note-system/framework/src/main/java/com/littlewin/framework/config/SecurityConfig.java

修改 filterChain 方法中的 authorizeHttpRequests 部分：

原代码：
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/auth/login","/api/admin/auth/logout", "/api/wx/auth/login").permitAll()
    .requestMatchers("/api/wx/user/files/**").permitAll()
    .requestMatchers("/api/wx/user/avatar").permitAll()
    .anyRequest().authenticated()
);

改为：
.authorizeHttpRequests(auth -> auth
    // 认证接口
    .requestMatchers("/api/admin/auth/login", "/api/admin/auth/logout").permitAll()
    .requestMatchers("/api/wx/auth/login", "/api/wx/auth/logout").permitAll()

    // 公开资源接口
    .requestMatchers("/api/wx/user/files/**").permitAll()
    .requestMatchers("/api/wx/user/avatar").permitAll()

    // 行为日志上报（无需登录即可上报）
    .requestMatchers("/api/wx/log/behavior").permitAll()

    // 微信回调接口（微信服务器推送，无 Token）
    .requestMatchers("/api/wx/callback/**").permitAll()

    // Knife4j 接口文档
    .requestMatchers("/doc.html").permitAll()
    .requestMatchers("/webjars/**").permitAll()
    .requestMatchers("/v3/api-docs/**").permitAll()
    .requestMatchers("/swagger-resources/**").permitAll()
    .requestMatchers("/favicon.ico").permitAll()

    // 其他所有请求需要认证
    .anyRequest().authenticated()
);

验证步骤：
1. 启动应用
2. 浏览器访问 http://localhost:8080/doc.html → 预期显示 Knife4j 文档页面
3. 不带 Token 访问 /api/wx/log/behavior → 预期不被 401 拦截
4. 不带 Token 访问其他受保护接口 → 预期 401
```

---

# ⏱️ Day 7 执行顺序

| 顺序 | 提示词 | 前置依赖 | 预计耗时 |
|:--:|:---|:--:|:--:|
| 1️⃣ | 提示词 1：Docker 部署 Redis | 无 | 15 分钟 |
| 2️⃣ | 提示词 2：Spring Boot 集成 Redis | 提示词 1 | 45 分钟 |
| 3️⃣ | 提示词 3：JWT 安全优化 | 提示词 2 | 60 分钟 |
| 4️⃣ | 提示词 4：SecurityConfig 放行路径 | 无 | 10 分钟 |

> 提示词 1-2 是基础，必须先完成。提示词 3 是核心改动，涉及 JwtUtils 重构 + 黑名单。提示词 4 独立可随时执行。
> 
> ⚠️ 提示词 3 改动范围较大（JwtUtils 从静态改为实例方法，所有调用处都要改），请仔细检查所有引用 JwtUtils 的文件，确保没有遗漏的静态调用。

---

# 🔍 提示词 3 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| JwtUtils.java | 重写 | 静态方法 → 实例方法，新增 jti/剩余有效期 |
| JwtAuthenticationFilter.java | 重写 | 注入 JwtUtils + RedisService，增加黑名单校验 |
| AdminAuthServiceImpl.java | 修改 | jwtUtils 实例调用 + logout 写黑名单 |
| WxAuthController.java | 修改 | logout 写黑名单 |
| WxUserServiceImpl.java | 修改 | JwtUtils.createToken → jwtUtils.createToken |
| ServletUtils.java | 新增方法 | getTokenFromRequest() |
| application.yml | 修改 | jwt.secret 改为占位符 |
| application-dev.yml | 修改 | 添加 64 字符密钥 |





# 实现后，不符合规范改进版提示词

## 提示词 1：JWT 密钥加固

````
# 任务：加固 JWT 密钥

## 背景
当前 `application-dev.yml` 中 JWT 密钥为明文弱密钥 `smart-note-secret-key`，存在被暴力破解的风险。HS512 算法要求密钥至少 64 字节（512 bit）才能保证签名强度。

## 要求

### 1. 生成安全密钥
在 `application-dev.yml` 中，将 `jwt.secret` 的值替换为一个 Base64 编码的 64 字节随机密钥。
示例格式（请自行生成新的，勿照抄）：
```yaml
jwt:
  secret: a3F4V2Z5YjdyMU9oNHBsNkdnZ0lXb3p6Q1ZtYm5Sa2R4ZXVpN0xqdz09
```

### 2. 同步修改 JwtUtils 签名方式
当前 `JwtUtils.java` 使用 `signWith(SignatureAlgorithm.HS512, SECRET)`，其中第二个参数接收 String，内部会按 UTF-8 编码取字节，若密钥长度不足会被截断或补零，存在安全隐患。

改为使用 `Keys.hmacShaKeyFor()` 方式：
- 引入 `javax.crypto.SecretKey` 和 `io.jsonwebtoken.security.Keys`
- 新增一个静态 `SecretKey` 字段 `KEY`
- 在 `@PostConstruct init()` 中通过 `Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))` 初始化
- `createToken` 改用 `.signWith(KEY)`
- `getSubject` 和 `getRemainingExpiration` 改用 `Jwts.parserBuilder().setSigningKey(KEY).build()` 解析

### 3. 涉及文件
- `admin/src/main/resources/application-dev.yml` — 替换密钥值
- `common/src/main/java/com/littlewin/common/utils/JwtUtils.java` — 改签名/解析方式


````

## 提示词 2：Token 添加 jti（JWT ID）

````
# 任务：为 JWT Token 添加唯一标识 jti

## 背景
当前 Token 黑名单以完整 token 字符串作为 Redis key，存在以下问题：
1. Token 字符串过长（通常 200+ 字符），浪费 Redis 内存
2. 无法通过 jti 快速定位和撤销特定 Token
3. 不符合 JWT 规范最佳实践（RFC 7519 推荐 jti 声明）

## 要求

### 1. JwtUtils.createToken 添加 jti 声明
在 `JwtUtils.java` 的 `createToken` 方法中：
- 引入 `java.util.UUID`
- 在 builder 链中添加 `.setId(UUID.randomUUID().toString())`
- 方法返回值改为返回一个包含 token 和 jti 的对象，或新增一个 `getTokenId(String token)` 方法用于从 token 中提取 jti

推荐方案：新增方法，保持 createToken 返回 String 不变：
```java
public static String createToken(String subject) {
    return Jwts.builder()
            .setSubject(subject)
            .setId(UUID.randomUUID().toString())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
            .signWith(KEY)
            .compact();
}

public static String getTokenId(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.getId();
}
```

### 2. 修改黑名单 key 策略
将所有使用完整 token 作为 Redis key 的地方，改为使用 jti：

**JwtAuthenticationFilter.java**：
```java
// 之前：stringRedisTemplate.hasKey(REDIS_KEY_PREFIX + token)
// 之后：stringRedisTemplate.hasKey(REDIS_KEY_PREFIX + JwtUtils.getTokenId(token))
```

**AdminAuthController.java**：
```java
// 之前：stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + token, "1", ...)
// 之后：String jti = JwtUtils.getTokenId(token);
//       stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + jti, "1", ...)
```

**WxAuthController.java**：同 AdminAuthController 改法

### 3. 涉及文件
- `common/src/main/java/com/littlewin/common/utils/JwtUtils.java`
- `framework/src/main/java/com/littlewin/framework/filter/JwtAuthenticationFilter.java`
- `system/src/main/java/com/littlewin/system/controller/AdminAuthController.java`
- `system/src/main/java/com/littlewin/system/controller/WxAuthController.java`


````



## 提示词 3：Redis 安全加固与连接池

````
# 任务：Redis 密码配置 + 连接池依赖

## 背景
当前 Redis 无密码保护，且缺少连接池依赖（commons-pool2），生产环境下存在安全风险和连接管理问题。

## 要求

### 1. 为 Docker Redis 设置密码
在 Docker 中重新启动 Redis 容器，添加密码参数：
```bash
docker run -d --name redis -p 6379:6379 --restart=always redis:7.2 redis-server --requirepass your_redis_password
```
注意：请将 `your_redis_password` 替换为123456，并牢记。

### 2. 修改 application-dev.yml
在 Redis 配置下添加 password 和连接池配置：
```yaml
data:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 2
        max-wait: -1ms
```

### 3. 添加 commons-pool2 依赖
在 `framework/pom.xml` 中，`spring-boot-starter-data-redis` 依赖下方添加：
```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```
注意：版本号由 Spring Boot 父 POM 管理，无需指定。

### 4. 涉及文件
- `admin/src/main/resources/application-dev.yml`
- `framework/pom.xml`

````

## 提示词 4：SecurityConfig 白名单补全

````
# 任务：补全 SecurityConfig 放行路径

## 背景
当前 `SecurityConfig.java` 的 `authorizeHttpRequests` 中缺少以下必要路径的放行：
1. Knife4j 接口文档相关路径（否则访问 /doc.html 会被拦截）
2. 微信小程序回调接口（如有）

## 要求

### 1. 在 filterChain 方法中补充 permitAll 路径
在现有的 `.requestMatchers("/api/wx/user/avatar").permitAll()` 之后，`.anyRequest().authenticated()` 之前，添加：

```java
.requestMatchers(
    "/doc.html",
    "/webjars/**",
    "/v3/api-docs/**",
    "/swagger-resources/**",
    "/swagger-resources",
    "/favicon.ico"
).permitAll()
```

完整效果应为：
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/auth/login","/api/admin/auth/logout", "/api/wx/auth/login").permitAll()
    .requestMatchers("/api/wx/user/files/**").permitAll()
    .requestMatchers("/api/wx/user/avatar").permitAll()
    .requestMatchers(
        "/doc.html",
        "/webjars/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/swagger-resources",
        "/favicon.ico"
    ).permitAll()
    .anyRequest().authenticated()
)
```

### 2. 涉及文件
- `framework/src/main/java/com/littlewin/framework/config/SecurityConfig.java`

````

## 提示词 5：创建 RedisService 工具类

````
# 任务：创建 RedisService 工具类

## 背景
当前项目中 Redis 操作直接使用 `StringRedisTemplate`，分散在 Controller 和 Filter 中，存在以下问题：
1. Redis key 前缀硬编码，多处重复定义 `REDIS_KEY_PREFIX = "token:blacklist:"`
2. 缺乏统一的 Redis 操作封装，后续扩展（如验证码、缓存）需重复编写
3. 违反 DRY 原则

## 要求

### 1. 在 common 模块创建 RedisService
路径：`common/src/main/java/com/littlewin/common/redis/RedisService.java`

```java
@Component
public class RedisService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
    }
}
```

### 2. 在 common 模块创建 RedisKeyConstants
路径：`common/src/main/java/com/littlewin/common/constants/RedisKeyConstants.java`

```java
public class RedisKeyConstants {
    public static final String TOKEN_BLACKLIST = "token:blacklist:";
}
```

### 3. 替换所有直接使用 StringRedisTemplate 的地方

**JwtAuthenticationFilter.java**：
- 删除 `REDIS_KEY_PREFIX` 常量
- 注入 `RedisService` 替代 `StringRedisTemplate`
- `hasKey` 调用改为 `redisService.hasKey(RedisKeyConstants.TOKEN_BLACKLIST + jti)`

**AdminAuthController.java**：
- 删除 `REDIS_KEY_PREFIX` 常量
- 注入 `RedisService` 替代 `StringRedisTemplate`
- `set` 调用改为 `redisService.set(RedisKeyConstants.TOKEN_BLACKLIST + jti, "1", remaining, TimeUnit.MILLISECONDS)`

**WxAuthController.java**：同 AdminAuthController 改法

### 4. 涉及文件
- 新建：`common/src/main/java/com/littlewin/common/redis/RedisService.java`
- 新建：`common/src/main/java/com/littlewin/common/constants/RedisKeyConstants.java`
- 修改：`framework/src/main/java/com/littlewin/framework/filter/JwtAuthenticationFilter.java`
- 修改：`system/src/main/java/com/littlewin/system/controller/AdminAuthController.java`
- 修改：`system/src/main/java/com/littlewin/system/controller/WxAuthController.java`

````

## 提示词 6：黑名单逻辑下沉到 Service 层

````
# 任务：将 Token 黑名单逻辑从 Controller 下沉到 Service 层

## 背景
当前 Token 黑名单（加入 Redis）的逻辑直接写在 Controller 中，违反了分层架构原则。Controller 应只负责接收请求和返回响应，业务逻辑应由 Service 层处理。

## 要求

### 1. AdminAuthService 添加黑名单方法
在 `AdminAuthService` 接口中添加：
```java
void addTokenToBlacklist(String token);
```

在 `AdminAuthServiceImpl` 中实现：
```java
@Resource
private RedisService redisService;

@Override
public void addTokenToBlacklist(String token) {
    String jti = JwtUtils.getTokenId(token);
    long remaining = JwtUtils.getRemainingExpiration(token);
    if (remaining > 0) {
        redisService.set(RedisKeyConstants.TOKEN_BLACKLIST + jti, "1", remaining, TimeUnit.MILLISECONDS);
    }
}
```

### 2. WxUserService 添加黑名单方法
在 `WxUserService` 接口中添加：
```java
void addTokenToBlacklist(String token);
```

在 `WxUserServiceImpl` 中实现（同 AdminAuthServiceImpl）。

### 3. 简化 Controller 中的 logout 方法

**AdminAuthController.java**：
```java
@PostMapping("/logout")
public Result logout(HttpServletRequest request) {
    adminAuthService.logout();

    String header = request.getHeader(Constants.TOKEN_HEADER);
    if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
        String token = header.substring(Constants.TOKEN_PREFIX.length());
        adminAuthService.addTokenToBlacklist(token);
    }

    return Result.success("退出成功");
}
```

**WxAuthController.java**：
```java
@PostMapping("/logout")
public Result logout(HttpServletRequest request) {
    LogContext.setBusinessId(SecurityUtils.getLoginUser().getUserId());

    String header = request.getHeader(Constants.TOKEN_HEADER);
    if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
        String token = header.substring(Constants.TOKEN_PREFIX.length());
        wxUserService.addTokenToBlacklist(token);
    }

    return Result.success("退出成功");
}
```

### 4. 涉及文件
- `system/src/main/java/com/littlewin/system/service/AdminAuthService.java`
- `system/src/main/java/com/littlewin/system/service/impl/AdminAuthServiceImpl.java`
- `system/src/main/java/com/littlewin/system/service/WxUserService.java`
- `system/src/main/java/com/littlewin/system/service/impl/WxUserServiceImpl.java`
- `system/src/main/java/com/littlewin/system/controller/AdminAuthController.java`
- `system/src/main/java/com/littlewin/system/controller/WxAuthController.java`

````

## 提示词 7：RedisConfig 增强

````
# 任务：增强 RedisConfig，添加 Object RedisTemplate

## 背景
当前 `RedisConfig.java` 只注册了 `StringRedisTemplate`，后续业务场景（如缓存对象）需要能存取任意 Java 对象的 RedisTemplate。Spring Boot 默认自动配置的 RedisTemplate 使用 JDK 序列化，key 在 Redis 中会以乱码存储，不便于调试。

## 要求

### 1. 在 RedisConfig 中添加 RedisTemplate<String, Object>
配置 key 使用 String 序列化，value 使用 JSON 序列化：

```java
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jsonSerializer.setObjectMapper(om);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### 2. 需要的额外 import
```java
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
```

### 3. 涉及文件
- `framework/src/main/java/com/littlewin/framework/config/RedisConfig.java`

````
