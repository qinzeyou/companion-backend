package com.caiya.companion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.Team;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.dto.TeamAddRequest;
import com.caiya.companion.model.dto.TeamUpdateRequest;
import com.caiya.companion.model.qo.TeamListQO;
import com.caiya.companion.service.TeamService;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


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
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(teamAddRequest, loginUser);
        return ResultUtils.success(teamId);
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Integer teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(teamId);
        return ResultUtils.success(result);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        boolean result = teamService.updateById(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/list")
    public BaseResponse<List<Team>> listTeam(@RequestBody TeamListQO teamListQO) {
        if (teamListQO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamListQO, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

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
}
