package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.TeamAddRequest;
import com.caiya.companion.model.request.TeamJoinRequest;
import com.caiya.companion.model.request.TeamQuitRequest;
import com.caiya.companion.model.request.TeamUpdateRequest;
import com.caiya.companion.model.qo.TeamListQO;
import com.caiya.companion.model.vo.TeamUserVO;

import java.util.List;


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

    /**
     * 条件查询队伍列表
     *
     * @param teamListQO
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeam(TeamListQO teamListQO, boolean isAdmin);

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除（解散）队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean deleteTeam(Long teamId, User loginUser);
}
