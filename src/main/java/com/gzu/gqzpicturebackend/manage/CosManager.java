package com.gzu.gqzpicturebackend.manage;

import com.gzu.gqzpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 将文件上传到指定的存储桶中
     *
     * @param key 文件在存储桶中的对象键（即路径和文件名）
     * @param file 要上传的本地文件对象
     * @return 返回上传后的对象信息，包括ETag、版本ID等
     */
    public PutObjectResult putObject(String key, File file) {
        // 创建一个PutObjectRequest对象，指定上传的目标存储桶、对象键和本地文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 使用cosClient执行上传操作，并返回上传后的对象信息
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     * @param key
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key 文件在存储桶中的对象键（即路径和文件名）
     * @param file 要上传的本地文件对象
     * @return 返回上传后的对象信息，包括ETag、版本ID等
     */
    public PutObjectResult putPictureObject(String key, File file) {
        // 创建一个PutObjectRequest对象，指定上传的目标存储桶、对象键和本地文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视为一种对图片的处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回图片信息，0 表示不返回
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);

        return cosClient.putObject(putObjectRequest);
    }
}
