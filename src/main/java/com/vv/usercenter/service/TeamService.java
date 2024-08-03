package com.vv.usercenter.service;

import com.vv.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.model.dto.TeamQuery;
import com.vv.usercenter.model.request.TeamUpdateRequest;
import com.vv.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
 * @author zx
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-07-20 10:40:32
 */
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    List<TeamUserVO> selectTeam(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);
}
