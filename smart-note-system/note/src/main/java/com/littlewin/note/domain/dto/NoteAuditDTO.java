package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteAuditDTO {
    @NotNull(message = "审核状态不能为空")
    private Integer status;
}
