package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.TeamMapper;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTeam;
import com.caiya.companion.model.dto.TeamAddRequest;
import com.caiya.companion.model.enums.TeamStatusEnum;
import com.caiya.companion.service.TeamService;
import com.caiya.companion.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Administrator
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2024-07-08 21:20:47
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addTeam(TeamAddRequest teamAddRequest, User loginUser) {
        //  1. 请求参数是否为空
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final long userId = loginUser.getId();
        //  2. 队伍人数 > 1 且 <= 20
        Integer maxNum = teamAddRequest.getMaxNum();
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍最大人数不满足要求");
        }
        //  3. 队伍名称不能为空 且 队伍标题长度 <= 20
        String name = teamAddRequest.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不满足要求");
        }
        //  4. 描述长度 <= 512
        String description = teamAddRequest.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        //  5. status 是否公开（int）不传默认为0（公开）
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamAddRequest.getStatus());
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  6. 如果status是加密状态，一定要传密码，且密码长度 <= 32
        String password = teamAddRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不满足要求");
            }
        }
        //  7. 超时时间 > 当前时间
        Date expireTime = teamAddRequest.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期时间大于当前时间");
        }
        //  8. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前创建人已超出最大创建队伍数限制");
        }
        // 9. 插入队伍信息到队伍表
        Team team = new Team();
        team.setUserId(userId);
        BeanUtils.copyProperties(teamAddRequest, team);
        boolean saveTeamResult = this.save(team);
        if (!saveTeamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        long teamId = team.getId();
        // 10. 插入用户 - 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean saveUserTeamResult = userTeamService.save(userTeam);
        if (!saveUserTeamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }
}




