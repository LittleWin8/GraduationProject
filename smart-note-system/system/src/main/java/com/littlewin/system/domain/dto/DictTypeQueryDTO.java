package com.littlewin.system.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 字典类型查询请求参数对象
 */
@Data
public class DictTypeQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private Integer pageNum = 1;
    /** 每页条数 */
    private Integer pageSize = 10;

    /** 字典名称 (支持模糊查询) */
    private String dictName;
    /** 状态 (1 正常，0 禁用) */
    private Integer status;
}