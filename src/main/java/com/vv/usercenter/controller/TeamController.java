package com.vv.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.usercenter.common.BaseResponse;
import com.vv.usercenter.common.ErrorCode;
import com.vv.usercenter.exception.BusinessException;
import com.vv.usercenter.model.domain.Team;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.model.dto.TeamQuery;
import com.vv.usercenter.model.request.TeamUpdateRequest;
import com.vv.usercenter.model.vo.TeamUserVO;
import com.vv.usercenter.service.TeamService;
import com.vv.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author vv先森
 * @create 2024-07-20 10:47
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long teamID = teamService.addTeam(team, loginUser);
        return new BaseResponse<>(0, teamID, "ok");
    }

    @DeleteMapping()
    public BaseResponse<Boolean> deleteTeam(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (result) {
            throw new BusinessException(ErrorCode.Fail_DELETE);
        }
        return new BaseResponse<>(0, true, "ok");
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean save = teamService.updateTeam(team, loginUser);
        if (!save) {
            throw new BusinessException(ErrorCode.Fail_UPDATE);
        }
        return new BaseResponse<>(0, true, "ok");
    }

    @GetMapping("/get")
    public BaseResponse<Team> getById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return new BaseResponse<>(0, team, "ok");
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getList(HttpServletRequest request, TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamUserVOS = teamService.selectTeam(teamQuery, isAdmin);
        if (teamUserVOS == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return new BaseResponse<>(0, teamUserVOS, "ok");
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> getPage(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> page = teamService.page(teamPage, teamQueryWrapper);
        if (page == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return new BaseResponse<>(0, page, "ok");

    }


}
