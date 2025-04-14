package com.gzu.gqzpicturebackend.aop;

import com.gzu.gqzpicturebackend.annotation.AuthCheck;
import com.gzu.gqzpicturebackend.exception.BusinessException;
import com.gzu.gqzpicturebackend.exception.ErrorCode;
import com.gzu.gqzpicturebackend.model.entity.User;
import com.gzu.gqzpicturebackend.model.enums.UserRoleEnum;
import com.gzu.gqzpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class AuthInterceptor {


    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        System.out.println("Must role enum: " + mustRoleEnum);
        // 如果不需要权限，放行
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        // 必须有权限才会通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限验证，放行
        return joinPoint.proceed();
    }
}
