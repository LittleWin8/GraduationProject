package com.littlewin.note.domain.dto;

import lombok.Data;

@Data
public class CategoryDTO {

    private String name;

    private Long parentId = 0L;

    private Integer sortOrder = 0;

    private Integer status;
}
