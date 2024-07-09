package com.caiya.companion.model.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author caiya
 * @description 加入队伍请求体
 * @create 2024-07-08 21:31
 */
@Data
public class TeamJoinRequest {
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
