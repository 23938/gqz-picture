package com.gzu.gqzpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzu.gqzpicturebackend.exception.ErrorCode;
import com.gzu.gqzpicturebackend.exception.ThrowUtils;
import com.gzu.gqzpicturebackend.manage.FileManager;
import com.gzu.gqzpicturebackend.model.dto.file.UploadPictureResult;
import com.gzu.gqzpicturebackend.model.dto.picture.PictureQueryRequest;
import com.gzu.gqzpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gzu.gqzpicturebackend.model.entity.Picture;
import com.gzu.gqzpicturebackend.model.entity.User;
import com.gzu.gqzpicturebackend.model.vo.PictureVO;
import com.gzu.gqzpicturebackend.model.vo.UserVO;
import com.gzu.gqzpicturebackend.service.PictureService;
import com.gzu.gqzpicturebackend.mapper.PictureMapper;
import com.gzu.gqzpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 86185
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-04-15 23:03:39
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;


    @Resource
    private UserService userService;

    /**
     * 校验图片信息
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片信息不能为空");
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id不能为空，有参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片id不能为空");
        // 如果传递了url再校验
        if (StrUtil.isNotBlank(url)){
            // 校验url
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url过长");
        }
        // 如果传递了url再校验
        if (StrUtil.isNotBlank(introduction)){
            // 校验url
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    /**
     * 上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }

        // 如果是更新的话，判断图片是否存在
        if (pictureId != null){
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        }
        // 上传图片，得到图片信息
        // 按照用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile,uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        // 操作数据库
        // 如果pictureId不为空，表示更新，否则是新增
        if (pictureId != null){
            // 如果是更新，需要补充id和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"图片上传失败，数据库操作失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取VO对象
     * @param picture
     * @return
     */
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 根据图片实体分页对象获取图片VO分页对象
     *
     * @param picturePage 图片实体分页对象，包含分页信息和图片实体列表
     * @param request HTTP请求对象，用于可能的请求上下文信息
     * @return 图片VO分页对象，包含转换后的图片VO列表和分页信息
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        // 获取图片列表
        List<Picture> pictureList = picturePage.getRecords();
        // 创建一个新的图片VO分页对象，初始化当前页码、页面大小和总记录数
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());

        // 检查图片列表是否为空
        if (CollUtil.isEmpty(pictureList)){
            // 如果列表为空，直接返回空的图片VO分页对象
            return pictureVOPage;
        }

        // 将图片实体列表转换为图片VO列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());

        // 提取所有图片的用户ID，并去除重复值
        Set<Long> userIdSet = pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());

        // 根据用户ID列表查询所有相关用户，并按用户ID分组
        Map<Long,List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 为每个图片VO设置关联的用户信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });

        // 将转换后的图片VO列表设置到图片VO分页对象中
        pictureVOPage.setRecords(pictureVOList);

        // 返回填充完毕的图片VO分页对象
        return pictureVOPage;
    }


    /**
     * 创建一个QueryWrapper对象用于查询图片信息
     * 此方法根据传入的查询请求参数，构建一个用于查询图片信息的QueryWrapper对象
     * 它处理各种查询条件和排序要求，以生成一个高效的查询
     *
     * @param pictureQueryRequest 包含查询条件和排序信息的请求对象
     * @return 返回一个构建好的QueryWrapper对象，用于执行查询
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        // 初始化QueryWrapper对象
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，则直接返回空的QueryWrapper对象
        if (pictureQueryRequest == null){
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();

        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 从多字段搜索
        if (StrUtil.isBlank(searchText)){
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name",searchText)
                    .or()
                    .like("introduction",searchText));
        }
        // 构建查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(StrUtil.isNotBlank(name),"name",name);
        queryWrapper.eq(StrUtil.isNotBlank(introduction),"introduction",introduction);
        queryWrapper.eq(StrUtil.isNotBlank(picFormat),"picFormat",picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category),"category",category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth),"picWidth",picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight),"picHeight",picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize),"picSize",picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale),"picScale",picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)){
            for (String tag : tags){
                queryWrapper.like("tags","\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                sortOrder.equals("ascend"),
                sortField);
        // 返回构建好的QueryWrapper对象
        return queryWrapper;
    }
}




