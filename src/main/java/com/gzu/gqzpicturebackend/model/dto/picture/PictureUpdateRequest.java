package com.gzu.gqzpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 */
@Data
public class PictureUpdateRequest implements Serializable {
 
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片介绍
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;  
}