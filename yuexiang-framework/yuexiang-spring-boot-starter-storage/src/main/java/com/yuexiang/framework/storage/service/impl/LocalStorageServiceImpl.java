package com.yuexiang.framework.storage.service.impl;

import com.yuexiang.framework.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.path:./uploads}")
    private String storagePath;

    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        Path path = Paths.get(storagePath, fileName);
        
        // 创建目录
        Files.createDirectories(path.getParent());
        
        // 保存文件
        file.transferTo(path);
        
        return fileName;
    }

    @Override
    public String upload(InputStream inputStream, String fileName) throws IOException {
        String newFileName = generateFileName(fileName);
        Path path = Paths.get(storagePath, newFileName);
        
        // 创建目录
        Files.createDirectories(path.getParent());
        
        // 保存文件
        Files.copy(inputStream, path);
        
        return newFileName;
    }

    @Override
    public void delete(String filePath) {
        Path path = Paths.get(storagePath, filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 忽略删除失败的情况
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }

    private String generateFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
}
