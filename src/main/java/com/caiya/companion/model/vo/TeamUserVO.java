package com.caiya.companion.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author caiya
 * @description 队伍列表返回包装类
 * @create 2024-07-09 08:11
 */
@Data
public class TeamUserVO {

    /**
     * 主键
     */
    private Long id;

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


    /**
     * 创建人信息
     */
    private UserVO createUser;
}
