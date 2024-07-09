package com.caiya.companion.model.qo;

import com.caiya.companion.common.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

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
     * 主键
     */
    private Long id;

    /**
     * 搜索关键字
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
     * 过期时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
