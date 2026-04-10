package com.yuexiang.system.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "文件上传", description = "通用文件上传接口")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;

    @Operation(summary = "单文件上传", description = "上传图片等文件，返回相对路径")
    @PostMapping
    public CommonResult<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String relativePath = storageService.upload(file);
            String fullUrl = storageService.getFileUrl(relativePath);
            return CommonResult.success(fullUrl);
        } catch (IOException e) {
            return CommonResult.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量上传", description = "同时上传多个文件，返回相对路径列表")
    @PostMapping("/batch")
    public CommonResult<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String relativePath = storageService.upload(file);
                urls.add(storageService.getFileUrl(relativePath));
            } catch (IOException e) {
                return CommonResult.error(500, "文件上传失败: " + e.getMessage());
            }
        }
        return CommonResult.success(urls);
    }
}
