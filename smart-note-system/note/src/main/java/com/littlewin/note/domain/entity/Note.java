package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note implements Serializable {

    @TableId(value = "note_id", type = IdType.AUTO)
    private Long noteId;

    @TableField("user_id")
    private Long userId;

    @TableField("category_id")
    private Long categoryId;  // 从 Integer 改为 Long，与建表语句一致

    private String title;

    private String content;

    @TableField("is_public")
    private Integer isPublic;

    private Integer status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    private String summary;

    @TableLogic  // MyBatis-Plus 逻辑删除注解
    @TableField("del_flag")
    private Integer delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}