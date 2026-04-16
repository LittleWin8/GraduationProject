package com.littlewin.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    private static String defaultPassword;

    @Value("${system.user.default-password}")
    public void setDefaultPassword(String defaultPassword) {
        PasswordUtils.defaultPassword = defaultPassword;
    }

    public static String encodeDefaultPassword() {
        return encoder.encode(defaultPassword);
    }

    public static String encode(String password) {
        return encoder.encode(password);
    }

    public static boolean matches(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }
}
