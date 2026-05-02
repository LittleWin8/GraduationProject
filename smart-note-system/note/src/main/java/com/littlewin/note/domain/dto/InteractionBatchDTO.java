package com.littlewin.note.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InteractionBatchDTO {
    @NotEmpty(message = "笔记ID列表不能为空")
    private List<Long> noteIds;
}
