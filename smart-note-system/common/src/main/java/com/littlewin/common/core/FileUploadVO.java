package com.littlewin.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {

    private String url;

    private String fileName;

    private String originalName;

    private Long size;
}
