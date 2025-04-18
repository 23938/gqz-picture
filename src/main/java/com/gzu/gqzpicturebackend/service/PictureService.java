package com.gzu.gqzpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzu.gqzpicturebackend.model.dto.picture.PictureQueryRequest;
import com.gzu.gqzpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gzu.gqzpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gzu.gqzpicturebackend.model.entity.User;
import com.gzu.gqzpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86185
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-04-15 23:03:39
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                           User loginUser);

    /**
     * 获取图片包装类（单条）
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    /**
     * 获取查询对象
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);
}
