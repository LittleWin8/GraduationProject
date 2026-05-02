package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InteractionToggleDTO {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    @NotBlank(message = "互动类型不能为空")
    private String type;
}
