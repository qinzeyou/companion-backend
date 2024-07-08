package com.caiya.companion.model.qo;

import com.caiya.companion.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author caiya
 * @description 查询队伍列表请求体
 * @create 2024-07-08 21:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamListQO extends PageRequest {
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 队伍状态：0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
}
