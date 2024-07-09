package com.caiya.companion.model.request;

import lombok.Data;

/**
 * @author caiya
 * @description 用户退出队伍请求体
 * @create 2024-07-09 14:41
 */
@Data
public class TeamQuitRequest {

    /**
     * 队伍id
     */
    private Long teamId;
}
