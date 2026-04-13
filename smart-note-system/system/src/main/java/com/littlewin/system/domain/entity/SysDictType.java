package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType {
    private Long dictId;
    private String dictName;
    private String dictType;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
