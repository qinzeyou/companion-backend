package com.caiya.companion.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

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

    /**
     * 队伍封面
     */
    private String teamCover;

    /**
     * 已加入的用户人数
     */
    private Integer joinUserCount;

    /**
     * 已加入的用户列表
     */
    private List<UserVO> joinUserList;

    /**
     * 标记当前登录用户是否加入该队伍，true为已加入，false反之
     */
    private Boolean hasJoin;
}
