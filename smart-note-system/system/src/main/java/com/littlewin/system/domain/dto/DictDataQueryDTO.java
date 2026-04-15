package com.littlewin.system.domain.dto;

import lombok.Data;

@Data
public class DictDataQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String dictType;   // 必传，用于定位是哪个字典的详情
    private String dictLabel;  // 搜索：标签名称
    private Integer status;    // 搜索：状态
}
