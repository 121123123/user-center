package com.vv.usercenter.mapper;

import com.vv.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zx
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-07-07 10:29:58
* @Entity com.vv.usercenter.model.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




