package com.yuexiang.framework.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    /**
     * 上传文件
     */
    String upload(MultipartFile file) throws IOException;

    /**
     * 上传文件
     */
    String upload(InputStream inputStream, String fileName) throws IOException;

    /**
     * 删除文件
     */
    void delete(String filePath);

    /**
     * 获取文件URL
     */
    String getFileUrl(String filePath);
}
