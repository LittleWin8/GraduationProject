package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminNotificationDTO {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer type;

    private List<Long> userIds;

    private Long noteId;
}
