package com.vv.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.usercenter.common.ErrorCode;
import com.vv.usercenter.exception.BusinessException;
import com.vv.usercenter.model.domain.Team;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.model.domain.UserTeam;
import com.vv.usercenter.model.dto.TeamQuery;
import com.vv.usercenter.model.enums.TeamStatusEnum;
import com.vv.usercenter.model.request.TeamUpdateRequest;
import com.vv.usercenter.model.vo.TeamUserVO;
import com.vv.usercenter.model.vo.UserVO;
import com.vv.usercenter.service.TeamService;
import com.vv.usercenter.mapper.TeamMapper;
import com.vv.usercenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zx
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-07-20 10:40:32
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamServiceImpl userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }

        final long userID = loginUser.getId();

        // 校验队伍人数 1-20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }

        // 队伍名字数不多于20
        String name = team.getName();
        if (StringUtils.isEmpty(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名不符合要求");
        }

        // 描述不多于512
        String description = team.getDescription();
        if (StringUtils.isEmpty(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不符合要求");
        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }

        // 如果status处于加密状态，则设置密码
        String password = team.getPassword();
        if (TeamStatusEnum.TEAM_SECRET.equals(status)) {
            if (StringUtils.isEmpty(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不符合要求");
            }
        }

        // 超时时间 > 创建时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 创建时间");
        }

        // 当前用户不能有超过5个队伍
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userID);
        int count = this.count(teamQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍数目过多");
        }

        team.setId(null);
        team.setUserId(userID);
        boolean save = this.save(team);
        if (!save || team.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userID);
        userTeam.setCreateTime(new Date());
        userTeam.setTeamId(team.getId());

        boolean result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        return team.getId();
    }

    @Override
    public List<TeamUserVO> selectTeam(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null) {
                teamQueryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(searchText)) {
                teamQueryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (name != null) {
                teamQueryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (description != null) {
                teamQueryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null) {
                teamQueryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null) {
                teamQueryWrapper.eq("userId", userId);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.TEAM_PUBLIC;
            }
            if (!isAdmin && teamStatusEnum.equals(TeamStatusEnum.TEAM_PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            teamQueryWrapper.eq("status", teamStatusEnum.getCode());
        }

        // 不展示已过期的队伍
        teamQueryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        ArrayList<TeamUserVO> teamUserVOS = new ArrayList<>();
        List<Team> list = this.list(teamQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        for (Team team : list) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            teamUserVOS.add(teamUserVO);

        }
        return teamUserVOS;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamUpdateRequest.getId());
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!userService.isAdmin(loginUser) && team.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (teamStatusEnum != null && teamStatusEnum.equals(TeamStatusEnum.TEAM_SECRET)) {
            if (teamUpdateRequest.getPassword() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码！");
            }
        }
        BeanUtils.copyProperties(teamUpdateRequest, team);
        return this.updateById(team);
    }
}




