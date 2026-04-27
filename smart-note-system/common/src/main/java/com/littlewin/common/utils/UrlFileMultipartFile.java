package com.littlewin.common.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 从网络URL构建的MultipartFile实现
 * 用于将远程文件（如微信头像）转换为MultipartFile对象
 */
public class UrlFileMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    /**
     * 从URL创建MultipartFile
     * @param name 表单字段名
     * @param urlStr 文件URL
     * @throws IOException 网络读取失败时抛出
     */
    public UrlFileMultipartFile(String name, String urlStr) throws IOException {
        this.name = name;
        this.originalFilename = extractFileName(urlStr);
        this.contentType = getContentType(urlStr);

        // 下载文件内容
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream()) {
            this.content = inputStream.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.content == null || this.content.length == 0;
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        java.nio.file.Files.write(dest.toPath(), this.content);
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileName(String urlStr) {
        try {
            // 移除查询参数
            String pureUrl = urlStr;
            int queryIndex = pureUrl.indexOf('?');
            if (queryIndex >= 0) {
                pureUrl = pureUrl.substring(0, queryIndex);
            }

            // 提取最后一个路径段
            int slashIndex = pureUrl.lastIndexOf('/');
            String fileName = slashIndex >= 0 ? pureUrl.substring(slashIndex + 1) : pureUrl;

            // 如果文件名为空或没有扩展名，补充默认名称
            if (fileName == null || fileName.isEmpty()) {
                fileName = "wechat_avatar.jpg";
            } else if (!fileName.contains(".")) {
                fileName = fileName + ".jpg";
            }

            return fileName;
        } catch (Exception e) {
            return "wechat_avatar.jpg";
        }
    }

    /**
     * 获取内容类型
     */
    private String getContentType(String urlStr) {
        String fileName = extractFileName(urlStr);
        String suffix = FileUploadUtils.getFileSuffix(fileName);

        switch (suffix.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "image/jpeg"; // 默认
        }
    }
}