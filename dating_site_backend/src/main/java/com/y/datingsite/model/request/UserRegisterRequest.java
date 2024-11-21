package com.y.datingsite.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 * @author : Yuan
 * @date :2024/4/19
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7359487837191478713L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
