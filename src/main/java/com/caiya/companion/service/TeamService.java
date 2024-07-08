package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.dto.TeamAddRequest;


/**
* @author Administrator
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-07-08 21:20:47
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param loginUser
     * @return
     */
    long addTeam(TeamAddRequest teamAddRequest, User loginUser);
}
