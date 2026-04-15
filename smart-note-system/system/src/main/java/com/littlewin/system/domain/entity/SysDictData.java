package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData {
    @TableId(type = IdType.AUTO)
    private Long dataId;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String tagType; // 对应 Geeker-admin 的 success/info/warning/danger
    private Integer sortOrder;
    private Integer status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}