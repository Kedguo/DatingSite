package com.y.datingsite.model.request;




import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Yuan
 * @date :2024/8/15
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;
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
     * 描述
     */
    private String description;
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 密码
     */
    private String password;


}
