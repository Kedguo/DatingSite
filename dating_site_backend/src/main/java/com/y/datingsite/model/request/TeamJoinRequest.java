package com.y.datingsite.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求对象
 * 用于封装用户加入队伍时所需的数据。
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3618557907313533138L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}