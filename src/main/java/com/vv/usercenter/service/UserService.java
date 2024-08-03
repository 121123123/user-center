package com.vv.usercenter.service;

import com.vv.usercenter.contant.UserConstant;
import com.vv.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author zx
 * @description 针对表【user】的数据库操作Service
 * @createDate 2024-07-07 10:29:58
 */
public interface UserService extends IService<User> {
    /**
     * 用戶登簬功能
     *
     * @param userAccount   用戶名
     * @param password      密碼
     * @param checkPassword 確認密碼
     * @return 用戶狀態
     */
    long UserRegister(String userAccount, String password, String checkPassword, String planetCode);

    User UserLogin(String userAccount, String password, HttpServletRequest request);

    User safetyUser(User user);

    int UserLogout(HttpServletRequest request);


    List<User> SearchUsersByTags(List<String> tagNameList);

    int userUpdate(User user,User loginUser);

    User getLoginUser(HttpServletRequest request);

    Boolean isAdmin(HttpServletRequest request);

    Boolean isAdmin(User user);
}
