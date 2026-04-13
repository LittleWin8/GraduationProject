package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData {
    private Long dataId;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String tagType; // 对应 Geeker-admin 的 success/info/warning/danger
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}