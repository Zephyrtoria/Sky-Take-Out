package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

import static com.sky.constant.MessageConstant.UPLOAD_FAILED;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    @PostMapping("upload")
    @ApiOperation("文件上传功能")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传中...: {}", file);
        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名的后缀  xx.png
            String extensionName = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构造新的文件名
            String objectName = UUID.randomUUID().toString() + extensionName;
            // 上传文件并获取文件访问路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败");
        }
        return Result.error(UPLOAD_FAILED);
    }
}
