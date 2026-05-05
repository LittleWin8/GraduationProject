package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.core.Upload;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.utils.FileUploadUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/file")
public class AdminFileController {

    @Resource
    private Upload uploadConfig;

    @PostMapping("/upload/img")
    @Log(module = LogModule.USER, action = LogAction.CREATE, desc = "上传图片")
    public Result<Map<String, String>> uploadImg(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择要上传的文件");
        }
        if (file.getSize() > uploadConfig.getMaxAvatarSize()) {
            throw new ServiceException("图片大小不能超过 " + (uploadConfig.getMaxAvatarSize() / 1024 / 1024) + "MB");
        }

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String suffix = FileUploadUtils.getFileSuffix(originalName).toLowerCase();
        if (!uploadConfig.getAllowedImageSuffixes().contains(suffix)) {
            throw new ServiceException("仅支持 " + String.join("、", uploadConfig.getAllowedImageSuffixes()) + " 图片格式");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException("非法文件类型");
        }

        String relativePath = FileUploadUtils.upload(
                file,
                uploadConfig.getLocalRootPath(),
                uploadConfig.getUserAvatarPath(),
                "web"
        );
        String url = "/api/wx/user/files" + relativePath;

        return Result.success(Map.of("fileUrl", url));
    }
}
