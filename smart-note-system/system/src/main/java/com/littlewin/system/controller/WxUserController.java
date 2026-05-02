package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.core.Upload;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.system.domain.dto.WxUserUpdateDTO;
import com.littlewin.common.core.FileUploadVO;
import com.littlewin.system.domain.vo.UserInfoVO;
import com.littlewin.system.service.WxUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/user")
public class WxUserController {
    private final WxUserService wxUserService;
    private final Upload uploadConfig;

    @GetMapping
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(wxUserService.getUserInfo());
    }

    @PutMapping
    @Log(module = LogModule.USER, action = LogAction.UPDATE, desc = "修改小程序用户个人资料")
    public Result<UserInfoVO> updateUserInfo(@RequestBody WxUserUpdateDTO dto) {
        return Result.success(wxUserService.updateUserInfo(dto));
    }

    @PostMapping("/avatar")
    @Log(module = LogModule.USER, action = LogAction.UPDATE, desc = "上传小程序用户头像")
    public Result<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(wxUserService.uploadAvatar(file));
    }

    /**
     * 读取本地资源文件
     */
    @GetMapping("/files/{category}/{type}/{date}/{fileName:.+}")
    public ResponseEntity<Resource> getLocalFile(@PathVariable String category,
                                                 @PathVariable String type,
                                                 @PathVariable String date,
                                                 @PathVariable String fileName) {
        // 1. 从配置类动态获取根路径
        Path rootPath = Paths.get(uploadConfig.getLocalRootPath());
        Path filePath = rootPath.resolve(category).resolve(type).resolve(date).resolve(fileName).normalize();

        // 2. 安全校验：防止路径穿越攻击
        if (!filePath.startsWith(rootPath)) {
            throw new ServiceException("文件路径不合法");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ServiceException("文件不存在");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            throw new ServiceException("文件访问失败");
        }
    }
}
