package com.y.datingsite.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 状态0正常
     */
    private Integer status;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 最大人数
     */
    private  Integer maxNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 描述
     */
    private String description;
    /**
     * 过期时间
     */
    /*@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    /**
     * 是否删除
     */
    @TableLogic //添加该注解只会把数据库的状态从0改成1，并不会真正的删除
    private Integer isDelete;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
