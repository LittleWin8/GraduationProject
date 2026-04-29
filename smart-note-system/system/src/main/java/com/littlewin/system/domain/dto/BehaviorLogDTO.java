package com.littlewin.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 行为日志上报请求
 */
@Data
public class BehaviorLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 行为类型：view / search */
    private String type;

    /** 行为内容：浏览时传笔记ID，搜索时传关键词 */
    private String content;
}
