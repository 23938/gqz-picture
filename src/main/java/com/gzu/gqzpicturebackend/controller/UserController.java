package com.gzu.gqzpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzu.gqzpicturebackend.annotation.AuthCheck;
import com.gzu.gqzpicturebackend.common.BaseResponse;
import com.gzu.gqzpicturebackend.common.DeleteRequest;
import com.gzu.gqzpicturebackend.common.ResultUtils;
import com.gzu.gqzpicturebackend.constant.UserConstant;
import com.gzu.gqzpicturebackend.exception.BusinessException;
import com.gzu.gqzpicturebackend.exception.ErrorCode;
import com.gzu.gqzpicturebackend.exception.ThrowUtils;
import com.gzu.gqzpicturebackend.model.dto.user.*;
import com.gzu.gqzpicturebackend.model.entity.User;
import com.gzu.gqzpicturebackend.model.vo.LoginUserVo;
import com.gzu.gqzpicturebackend.model.vo.UserVO;
import com.gzu.gqzpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVo loginUserVo = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVo(loginUser));
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        userService.userLogout(request);
        return ResultUtils.success(true);
    }

    /**
     * 创建用户
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码
        final String DEFALUT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFALUT_PASSWORD);
        user.setUserPassword(encryptPassword);
        // 插入数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户(管理员)
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVOById(long id){
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        System.out.println(user);
        return ResultUtils.success(true);
    }

    /**
     * 根据页面请求获取用户VO列表
     *
     * @param userQueryRequest 用户查询请求，包含分页信息和查询条件
     * @return 包含用户VO页面信息的响应对象
     *
     * 此方法用于处理用户信息的分页查询请求，将查询到的用户信息转换为用户VO，
     * 并以分页的形式返回给调用者
     */
    @PostMapping("/lisy/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 从请求参数中获取当前页码和页面大小
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        // 调用userService的page方法，根据当前页码、页面大小和查询条件获取用户页面
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));

        // 初始化用户VO页面对象，设置总记录数
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());

        // 调用userService的getUserVOList方法，将用户实体列表转换为用户VO列表
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());

        // 将用户VO列表设置到用户VO页面对象中
        userVOPage.setRecords(userVOList);

        // 返回包含用户VO页面信息的成功响应对象
        return ResultUtils.success(userVOPage);
    }
}
