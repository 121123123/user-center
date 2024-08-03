package com.vv.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.usercenter.model.domain.UserTeam;
import com.vv.usercenter.service.UserTeamService;
import com.vv.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author zx
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-20 10:42:31
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




