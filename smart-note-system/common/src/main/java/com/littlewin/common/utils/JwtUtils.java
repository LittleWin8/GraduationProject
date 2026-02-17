package com.littlewin.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    /**
     * 从配置文件读取
     */
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expire}")
    private Long expireTime;

    /**
     * 静态变量接收配置值
     */
    private static String SECRET;
    private static Long EXPIRE_TIME;

    @PostConstruct
    public void init() {
        SECRET = this.secretKey;
        EXPIRE_TIME = this.expireTime;
    }

    /**
     * 生成 token
     */
    public static String createToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 解析 token
     */
    public static String getSubject(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
