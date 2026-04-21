package com.littlewin.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlewin.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.Resource;

/**
 * 微信小程序 API 工具类
 */
@Slf4j
@Component
public class WechatApiUtils {

    @Value("${wx.mp.app-id}")
    private String appId;

    @Value("${wx.mp.secret}")
    private String secret;

    @Value("${wx.mp.api.code2session}")
    private String code2sessionUrl;

    @Resource
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过 code 获取 openid 和 session_key
     * @param code 微信登录 code
     * @return WechatSession 对象
     */
    public WechatSession getSessionByCode(String code) {
        String url = UriComponentsBuilder.fromHttpUrl(code2sessionUrl)
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();

        try {
            log.info("调用微信 code2session 接口, code: {}", code);
            String responseStr = restTemplate.getForObject(url, String.class);
            log.info("微信返回原始数据: {}", responseStr);

            JsonNode jsonNode = objectMapper.readTree(responseStr);

            // 检查错误码
            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                int errCode = jsonNode.get("errcode").asInt();
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信接口返回错误: errcode={}, errmsg={}", errCode, errMsg);
                throw new ServiceException("微信登录失败: " + errMsg);
            }

            // 提取数据
            String openid = jsonNode.get("openid").asText();
            String sessionKey = jsonNode.has("session_key") ? jsonNode.get("session_key").asText() : "";

            log.info("获取成功 - openid: {}, session_key: {}", openid, sessionKey);

            return WechatSession.builder()
                    .openid(openid)
                    .sessionKey(sessionKey)
                    .build();

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new ServiceException("微信服务器请求失败: " + e.getMessage());
        }
    }

    /**
     * 微信 session 对象
     */
    public static class WechatSession {
        private String openid;
        private String sessionKey;

        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private WechatSession session = new WechatSession();
            public Builder openid(String openid) { session.openid = openid; return this; }
            public Builder sessionKey(String sessionKey) { session.sessionKey = sessionKey; return this; }
            public WechatSession build() { return session; }
        }
    }
}