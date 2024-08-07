package com.caiya.companion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.*;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.TeamAddRequest;
import com.caiya.companion.model.request.TeamJoinRequest;
import com.caiya.companion.model.request.TeamQuitRequest;
import com.caiya.companion.model.request.TeamUpdateRequest;
import com.caiya.companion.model.qo.TeamListQO;
import com.caiya.companion.model.vo.TeamUserVO;
import com.caiya.companion.service.TeamService;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * @author caiya
 * @description 队伍控制层
 * @create 2024-05-05 09:02
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long teamId = teamService.addTeam(teamAddRequest);
        return ResultUtils.success(teamId);
    }


    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 条件查询队伍列表
     *
     * @param teamListQO 查询请求体
     * @param request 登录信息
     * @return 条件分页查询的队伍数据
     */
    @PostMapping("/search/page")
    public BaseResponse<List<TeamUserVO>> listTeam(@RequestBody TeamListQO teamListQO, HttpServletRequest request) {
        if (teamListQO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamUserVOList = teamService.listTeam(teamListQO, request);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取分页推荐的队伍数据
     * @param pageRequest 分页参数
     * @return 队伍数据
     */
    @PostMapping("/recommend/page")
    public BaseResponse<PageResponse<List<TeamUserVO>>> recommendTeamList(@RequestBody PageRequest pageRequest, HttpServletRequest request) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageResponse<List<TeamUserVO>> response = teamService.recommendTeamList(pageRequest, request);
        return ResultUtils.success(response);
    }

    /**
     * 获取指定用户创建的队伍
     *
     * @param userId
     * @return
     */
    @GetMapping("/list/user/create")
    public BaseResponse<List<TeamUserVO>> listCreateTeamByUser(@RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamUserVOList = teamService.listCreateTeamByUser(userId);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取指定用户加入或创建的队伍
     *
     * @param userId
     * @return
     */
    @GetMapping("/list/user/manager/{userId}")
    public BaseResponse<Map<String, List<TeamUserVO>>> listTeamByUser(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Map<String, List<TeamUserVO>> result = teamService.listTeamByUser(userId);
        return ResultUtils.success(result);
    }

    /**
     * 获取简单分页队伍数据接口
     * @param teamListQO 查询请求体
     * @return 队伍分页数据
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(@RequestBody TeamListQO teamListQO) {
        if (teamListQO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamListQO, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamListQO.getPageNum(), teamListQO.getPageSize());
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @DeleteMapping("/delete/{teamId}")
    public BaseResponse<Boolean> deleteTeam(@PathVariable Long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(teamId, loginUser);
        return ResultUtils.success(result);
    }
}
