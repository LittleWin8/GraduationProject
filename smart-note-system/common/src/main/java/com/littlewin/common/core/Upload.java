package com.littlewin.common.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class Upload {
    private String localRootPath;
    private String userAvatarPath;
    private List<String> allowedImageSuffixes;
    private Long maxAvatarSize;
}