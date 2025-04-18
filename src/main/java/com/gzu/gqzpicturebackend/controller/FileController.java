package com.gzu.gqzpicturebackend.controller;

import com.gzu.gqzpicturebackend.annotation.AuthCheck;
import com.gzu.gqzpicturebackend.common.BaseResponse;
import com.gzu.gqzpicturebackend.common.ResultUtils;
import com.gzu.gqzpicturebackend.constant.UserConstant;
import com.gzu.gqzpicturebackend.exception.BusinessException;
import com.gzu.gqzpicturebackend.exception.ErrorCode;
import com.gzu.gqzpicturebackend.manage.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    private final CosManager cosManager;

    public FileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 测试上传文件
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 获取上传文件的原始名称
        String filename = multipartFile.getOriginalFilename();
        // 拼接文件的存储路径
        String filepath = String.format("/test/%s", filename);
        // 初始化File对象为null
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filepath, null);
            // 将上传的文件转移到临时文件中
            multipartFile.transferTo(file);
            // 将文件上传到目标存储系统
            cosManager.putObject(filepath, file);
            // 返回可访问的地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            // 记录文件上传过程中的错误日志
            log.error("file upload error, filepath = " + filepath, e);
            // 抛出业务异常，表示文件上传失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 判断File对象是否不为空
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                // 如果删除失败，则记录错误日志
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试下载文件
     *
     * @param filepath
     * @param response
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput  = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            // 抛出业务异常，表示文件下载失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            // 释放流
            if (cosObjectInput != null){
                cosObjectInput.close();
            }
        }

    }
}
