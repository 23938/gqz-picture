package com.gzu.gqzpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户请求类
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -8555794454954558382L;
    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
