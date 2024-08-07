package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.PageRequest;
import com.caiya.companion.common.PageResponse;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.TeamMapper;
import com.caiya.companion.mapper.UserMapper;
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
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.caiya.companion.constant.UserConstant.USER_LOGIN_STATE;

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
    @Resource
    private UserMapper userMapper;

    /**
     * 创建队伍
     *
     * @param teamAddRequest 请求体
     * @return 添加成功的队伍id
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public long addTeam(TeamAddRequest teamAddRequest) {
        // 判断用户是否存在
        Long userId = teamAddRequest.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建人不存在");
        //  1. 请求参数是否为空
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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
     * @param request
     * @return
     */
    @Override
    public List<TeamUserVO> listTeam(TeamListQO teamListQO, HttpServletRequest request) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 获取当前登录用户信息
        UserVO loginUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
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
            // 根据【队伍名称】查询
            String name = teamListQO.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 根据【队伍描述】查询
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
            // 根据队伍状态查询，只有管理员才能查看非公开的队伍
            Integer status = Optional.ofNullable(teamListQO.getStatus()).orElse(TeamStatusEnum.PUBLIC.getValue());
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            // 如果不是管理员 且 查询队伍的状态为非公开 则报错
            if (!userService.isAdmin(loginUser) && teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (teamStatusEnum == null) {
                queryWrapper.and(qw -> qw.eq("status", TeamStatusEnum.PUBLIC.getValue()).or().eq("status", TeamStatusEnum.SECRET.getValue()));
            } else {
                queryWrapper.eq("status", teamStatusEnum.getValue());
            }
        }
        // 不展示已过期的队伍
        queryWrapper.nested(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        // 条件查询队伍
        List<Team> teamList = this.list(queryWrapper);
        // 如果查询出队伍数据为空，则直接返回空数组
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 处理用户与队伍关系
        // 存储用户已加入的队伍id，用于过滤用户已加入的队伍
        List<Long> userTeamIdList = new ArrayList<>();
        if (loginUser != null) {
            // 获取当前登录用户加入的队伍
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 如果用户加入或创建的队伍不为空，则取出他加入或创建的队伍id
            if (CollectionUtils.isNotEmpty(userTeamList)) {
                for (UserTeam userTeam : userTeamList) {
                    userTeamIdList.add(userTeam.getTeamId());
                }
            }
        }
        // 脱敏队伍列表信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            // 获取创建人id
            Long userId = team.getUserId();
            if (userId == null) continue;
            // 创建人信息
            User user = userService.getById(userId);
            // 脱敏队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 用户信息脱敏
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            // 设置该用户是否加入队伍的标识
            teamUserVO.setHasJoin(userTeamIdList.contains(team.getId()));
            // 获取该队伍加入的用户
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", team.getId());
            // 查询该队伍的用户队伍关系数据
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 该队伍已经加入的用户id
            List<Long> joinTeamUserIdList = userTeamList.stream().map(UserTeam::getUserId).collect(Collectors.toList());
            // 存储已加入队伍的脱敏后用户数据
            ArrayList<UserVO> joinUserVOS = new ArrayList<>();
            // 对已加入的用户数据进行脱敏
            for (Long joinUserId : joinTeamUserIdList) {
                User joinUser = userService.getById(joinUserId);
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(joinUser, userVO);
                joinUserVOS.add(userVO);
            }
            // 设置已加入的队伍列表数据
            teamUserVO.setJoinUserList(joinUserVOS);
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
        // 更新队伍加入用户数
        team.setJoinUserCount(team.getJoinUserCount() + 1);
        this.updateById(team);
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
     *
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
        // 更新队伍加入用户数
        team.setJoinUserCount(team.getJoinUserCount() - 1);
        this.updateById(team);
        // 移除 该队伍的 用户 - 队伍 关系
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * 删除（解散）队伍
     *
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
     * 获取指定用户创建的队伍
     *
     * @param userId
     * @return
     */
    @Override
    public List<TeamUserVO> listCreateTeamByUser(Long userId) {
        // 获取用户的信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 获取用户创建的队伍列表
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<Team> teamList = this.list(queryWrapper);

        // 信息脱敏
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            // 队伍信息脱敏
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);

            // 用户信息脱敏
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 获取指定用户加入或创建的队伍
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, List<TeamUserVO>> listTeamByUser(Long userId) {
        // 获取用户的信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 1. 获取用户创建的队伍列表
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userId);
        List<Team> teamList = this.list(teamQueryWrapper);

        // 返回结果：map键值对
        Map<String, List<TeamUserVO>> map = new HashMap<>();

        // 信息脱敏
        List<TeamUserVO> createTemaUserVOList = getTeamUserVOS(user, teamList);
        map.put("createTeamList", createTemaUserVOList);

        // 2. 获取用户加入的队伍
        // 查询条件
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        // 如果用户创建队伍不为0，才去重
        if (CollectionUtils.isNotEmpty(teamList)) {
            // 取出用户创建队伍的所有队伍id
            List<Long> createTeamIds = teamList.stream().map(Team::getId).collect(Collectors.toList());
            // 查询条件：userId为指定用户 且 teamId不在创建的队伍id数组中
            userTeamQueryWrapper.eq("userId", userId).notIn("teamId", createTeamIds);
        } else {
            // 以用户id作为查询条件
            userTeamQueryWrapper.eq("userId", userId);
        }
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        // 获取用户队伍关系中用户加入队伍的id
        List<Long> joinTeamIds = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        List<TeamUserVO> joinTeamUserVOList = new ArrayList<>();
        // 如果加入队伍不为0，才根据已加入的队伍id去查询队伍信息
        if (CollectionUtils.isNotEmpty(joinTeamIds)) {
            teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.in("id", joinTeamIds);
            List<Team> joinTeamList = this.list(teamQueryWrapper);
            // 信息脱敏
            joinTeamUserVOList = getTeamUserVOS(user, joinTeamList);
        }
        map.put("joinTeamList", joinTeamUserVOList);
        return map;
    }

    /**
     * 获取分页的队伍数据
     *
     * @param pageRequest 分页数据
     * @return 分页队伍数据
     */
    @Override
    public PageResponse<List<TeamUserVO>> recommendTeamList(PageRequest pageRequest, HttpServletRequest request) {
        // 获取分页队伍数据
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 不推荐已过期的队伍
//        queryWrapper.nested(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        Page<Team> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        Page<Team> teamPage = page(page, queryWrapper);
        List<Team> teamList = teamPage.getRecords();

        List<TeamUserVO> teamUserVOList = teamList.stream().map(team -> {
            // 根据 teamId 获取用户队伍信息关系表中队伍用户关联关系
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", team.getId());
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 获取关联关系中的用户Id
            List<Long> joinUserIdList = userTeamList.stream().map(UserTeam::getUserId).collect(Collectors.toList());
            // 获取用户的详细信息，并进行脱敏
            List<UserVO> userVoList = null;
            if (!userTeamList.isEmpty()) {
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.in("id", joinUserIdList);
                // 用户信息脱敏
                List<User> userList = userService.list(userQueryWrapper);
                userVoList = userList.stream().map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).collect(Collectors.toList());
            }
            // 队伍信息脱敏
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            teamUserVO.setJoinUserList(userVoList);
            return teamUserVO;
        }).collect(Collectors.toList());
        // 返回自定义封装分页结果
        PageResponse<List<TeamUserVO>> pageResponse = new PageResponse<>();
        pageResponse.setTotal(teamPage.getTotal());
        pageResponse.setCurrent(teamPage.getCurrent());
        pageResponse.setSize(teamPage.getSize());
        pageResponse.setRecords(teamUserVOList);

        return pageResponse;
    }

    /**
     * 用户、队伍信息脱敏
     *
     * @param user     创建人信息
     * @param teamList 队伍列表
     * @return 脱敏队伍列表
     */
    @NotNull
    private static List<TeamUserVO> getTeamUserVOS(User user, List<Team> teamList) {
        List<TeamUserVO> createTemaUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            // 队伍信息脱敏
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);

            // 用户信息脱敏
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            createTemaUserVOList.add(teamUserVO);
        }
        return createTemaUserVOList;
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




