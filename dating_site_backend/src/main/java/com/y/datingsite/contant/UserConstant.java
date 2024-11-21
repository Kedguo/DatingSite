package com.y.datingsite.contant;

/**
 * 用户常量类
 * @author : Yuan
 * @date :2024/4/19
 */
public interface UserConstant {


    /**
     * 用户登录态键，通过这个 key 可以找到 唯一的一条数据
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 权限：
     * DEFAULT_ROLE 普通用户（默认）
     * ADMIN_ROLE 管理员
     */
    int DEFAULT_ROLE = 0;
    int ADMIN_ROLE = 1;

}
