package com.gzu.gqzpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片标签分类
 */
@Data
public class PictureTagCategoryVO {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
