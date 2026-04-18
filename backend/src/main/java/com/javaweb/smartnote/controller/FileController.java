package com.javaweb.smartnote.controller;

import com.javaweb.smartnote.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传模块")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-path:uploads}")
    private String configuredUploadPath;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    private String uploadPath;

    @PostConstruct
    public void init() {
        String userDir = System.getProperty("user.dir");
        uploadPath = new File(userDir, configuredUploadPath).getAbsolutePath();
        log.info("文件上传目录: {}", uploadPath);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error(400, "文件名无效");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        if (!isImageFile(extension)) {
            return Result.error(400, "仅支持 jpg、jpeg、png、gif 格式的图片");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        File dir = new File(uploadPath + File.separator + "avatar" + File.separator + datePath.replace("/", File.separator));
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                log.error("无法创建目录: {}", dir.getAbsolutePath());
                return Result.error(500, "无法创建存储目录");
            }
        }

        try {
            File destFile = new File(dir, newFilename);
            file.transferTo(destFile.getAbsoluteFile());

            String avatarUrl = baseUrl + "/uploads/avatar/" + datePath + "/" + newFilename;
            log.info("头像上传成功: {}", avatarUrl);
            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error(500, "上传失败，请稍后重试");
        }
    }

    private boolean isImageFile(String extension) {
        String ext = extension.toLowerCase();
        return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".gif");
    }
}
