package com.y.datingsite.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求对象，用于表示分页查询的请求参数。
 * @ClassName: PageRequest
 * @apiNote 该类用于封装分页查询的请求参数，包括每页大小和当前页码。
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -8721085062505718474L;

    /**
     * 每页记录数，默认为 10。
     */
    protected int pageSize = 10;

    /**
     * 当前页码，默认为 1。
     */
    protected int pageNum = 1;
}