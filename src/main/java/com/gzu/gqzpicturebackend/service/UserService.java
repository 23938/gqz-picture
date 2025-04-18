package com.gzu.gqzpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gzu.gqzpicturebackend.model.dto.user.UserQueryRequest;
import com.gzu.gqzpicturebackend.model.dto.user.UserRegisterRequest;
import com.gzu.gqzpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gzu.gqzpicturebackend.model.vo.LoginUserVo;
import com.gzu.gqzpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86185
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-04-09 17:20:17
*/
public interface UserService extends IService<User> {

    /**
     * 用户请求
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取加密密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取脱敏后的登录用户信息
     * @param user
     * @return
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);
}
