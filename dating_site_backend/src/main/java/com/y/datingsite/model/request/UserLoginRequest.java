package com.y.datingsite.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 * @author : Yuan
 * @date :2024/4/19
 */
@Data //自动生成set()、get()方法
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -7359487837191478713L;

    public String userAccount;
    public String userPassword;
}
