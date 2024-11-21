package com.y.datingsite.model.dto;

import com.y.datingsite.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;


/**
 * 队伍查询对象，用于表示队伍的查询条件。
 * @ClassName: TeamQuery
 * @apiNote 该类继承自 PageRequest，用于分页查询队伍信息，并包含了队伍的各种查询条件。
 */
// 用于自动生成 equals() 和 hashCode() 方法会调用父类 PageRequest 的对应方法
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * id 列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}