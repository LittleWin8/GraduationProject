package com.littlewin.common.utils;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.core.FileUploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * 通用文件上传工具类
 * 负责将 MultipartFile 保存到本地磁盘，并按日期自动生成目录结构
 * 返回统一的访问相对路径，供 Controller 层映射为静态资源 URL
 */
@Slf4j
public class FileUploadUtils {

    /**
     * 通用上传方法
     *
     * @param file     上传的文件对象
     * @param rootPath 本地存储根路径（通常从配置类注入，如 /uploads）
     * @param subDir   业务子目录（如 user/avatar、note/attachment）
     * @param prefix   文件名前缀（通常为业务主键，如 userId，便于追溯）
     * @return 相对访问路径，格式如：/user/avatar/20260427/1001_xxx.jpg
     *         该路径可直接拼接到静态资源映射前缀后供前端访问
     * @throws ServiceException 文件保存失败或缺少后缀名时抛出业务异常
     */
    public static String upload(MultipartFile file, String rootPath, String subDir, String prefix) {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String suffix = getFileSuffix(originalName);

        // 1. 生成日期目录
        String dateDir = LocalDate.now().toString().replace("-", "");
        Path targetDir = Paths.get(rootPath, subDir, dateDir);

        // 2. 生成唯一文件名
        String fileName = prefix + "_" + UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        Path targetFile = targetDir.resolve(fileName);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new ServiceException("文件保存失败");
        }

        // 返回给前端访问的相对路径 (对应 Controller 的映射)
        return "/" + subDir + "/" + dateDir + "/" + fileName;
    }

    /**
     * 获取文件后缀（不含点），转小写
     *
     * @param fileName 文件名
     * @return 后缀名，如 jpg、png
     * @throws ServiceException 文件名无后缀时抛出业务异常
     */
    public static String getFileSuffix(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) throw new ServiceException("文件缺少后缀名");
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}