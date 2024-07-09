package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.TeamMapper;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTeam;
import com.caiya.companion.model.request.TeamAddRequest;
import com.caiya.companion.model.request.TeamJoinRequest;
import com.caiya.companion.model.request.TeamQuitRequest;
import com.caiya.companion.model.request.TeamUpdateRequest;
import com.caiya.companion.model.enums.TeamStatusEnum;
import com.caiya.companion.model.qo.TeamListQO;
import com.caiya.companion.model.vo.TeamUserVO;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.TeamService;
import com.caiya.companion.service.UserService;
import com.caiya.companion.service.UserTeamService;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private UserService userService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
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
        // todo 可能发生 100 个队伍同时插入的bug
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        team.setUserId(userId);
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

    /**
     * 条件查询队伍列表
     *
     * @param teamListQO
     * @return
     */
    @Override
    public List<TeamUserVO> listTeam(TeamListQO teamListQO, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamListQO != null) {
            // 根据队伍id查询
            Long teamId = teamListQO.getId();
            if (teamId != null && teamId > 0) {
                queryWrapper.eq("id", teamId);
            }
            // 通过搜索关键词同时对名称和描述查询
            String searchText = teamListQO.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名称查询
            String name = teamListQO.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 根据队伍描述查询
            String description = teamListQO.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 根据队伍最大人数查询
            Integer maxNum = teamListQO.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据队伍创建人查询
            Long userId = teamListQO.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据队伍状态查询，只有管理员才能查看加密还有非公开的队伍
            Integer status = teamListQO.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            // 如果不是管理员 且 查询队伍的状态为非公开 则报错
            if (!isAdmin && !teamStatusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", teamStatusEnum.getValue());
        }
        // 不展示已过期的队伍
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 脱敏队伍列表信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            // 获取创建人id
            Long userId = team.getUserId();
            if (userId == null) continue;
            User user = userService.getById(userId);
            User safetyUser = userService.getSafetyUser(user);
            // 脱敏队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(safetyUser, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        Long teamId = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(teamId);
        // 只有管理员或者队伍的创建者可以修改
        if (!userService.isAdmin(loginUser) || !Objects.equals(oldTeam.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 如果队伍状态该为加密，必须要有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        String password = teamUpdateRequest.getPassword();
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码满足要求");
            }
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        boolean result = this.updateById(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return true;
    }


    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        Long teamId = teamJoinRequest.getTeamId();
        Team team = this.getById(teamId);
        Long userId = loginUser.getId();

        // 队伍必须存在
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 队伍未过期
        Date expireTime = team.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 不能加入私有队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        // 如果加入的队伍是加密的，必须密码匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍密码错误");
            }
        }

        // 用户加入队伍的数据
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasUserJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
        // 用户加入队伍是否超出上限
        if (hasUserJoinTeamNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户加入队伍超出限制");
        }
        // 队伍是否已满
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        // 队伍中已加入的用户数量
        long hasTeamJoinUserNum = userTeamService.count(userTeamQueryWrapper);
        if (hasTeamJoinUserNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        // 不能重复加入队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        // 查询用户是否加入当前队伍
        long hasJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinTeamNum > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        // 数据操作
        // 新增用户 - 队伍关系
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        boolean saveUserTeamResult = userTeamService.save(userTeam);
        if (!saveUserTeamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增用户-队伍关联关系失败");
        }
        return true;
    }

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        Long teamId = teamQuitRequest.getTeamId();
        Long userId = loginUser.getId();
        // 队伍必须存在
        Team team = this.getTeamById(teamId);
        // 用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        // 根据 用户id 和 队伍id 查询队伍关系表，如果返回条数大于0，说明该用户已加入队伍
        long hasUserJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoinTeamNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未加入队伍");
        }
        // 获取队伍中已加入的用户数量
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        // 已加入的用户数量
        long teamJoinUserNum = userTeamService.count(queryWrapper);
        // 如果队伍只剩一人：解散
        if (teamJoinUserNum <= 1) {
            // 删除队伍信息
            boolean result = this.removeById(teamId);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍信息失败");
            }
        } else { // 队伍剩余超过1个，分情况处理
            // 如果是队长退出，解散队伍
            if (Objects.equals(team.getUserId(), userId)) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的最早的两个人
                QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
                teamQueryWrapper.eq("teamId", teamId);
                teamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(teamQueryWrapper);
                // 如果查出来的数组为空 或 数组长度 <= 1 说明该条数据有误
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 除队长外，最早加入的用户
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长信息失败");
                }
            }
        }
        // 移除 该队伍的 用户 - 队伍 关系
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * 删除（解散）队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public boolean deleteTeam(Long teamId, User loginUser) {
        long userId = loginUser.getId();
        // 校验队伍是否存在
        Team team = this.getTeamById(teamId);
        // 校验你是不是这个队伍的队长
        if (team.getUserId() != userId) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入这个队伍的关联关系
        QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(teamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "移除队伍关联关系失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 根据队伍id获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}




