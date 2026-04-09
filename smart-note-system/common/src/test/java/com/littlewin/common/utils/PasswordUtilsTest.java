package com.littlewin.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 密码工具类测试
 * 用于生成 BCrypt 密码或验证密码匹配逻辑
 */
class PasswordUtilsTest {

    @Test
    void testPasswordEncodeAndMatch() {
        // 1. 准备原始密码
        String rawPassword = "123456";

        // 2. 执行加密
        String encodedPassword = PasswordUtils.encode(rawPassword);

        // 3. 打印结果
        System.out.println("================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);

        // 4. 执行验证
        boolean match = PasswordUtils.matches(rawPassword, encodedPassword);
        System.out.println("是否匹配: " + match);
        System.out.println("================================");

        // 5. 断言（自动检查结果是否正确）
        if (!match) {
            throw new RuntimeException("密码比对失败！");
        }
    }
}