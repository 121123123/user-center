package com.vv.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vv.usercenter.common.ErrorCode;
import com.vv.usercenter.contant.UserConstant;
import com.vv.usercenter.exception.BusinessException;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.service.UserService;
import com.vv.usercenter.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.crypto.digest.DigestUtil.md5Hex;

/**
 * @author zx
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-07-07 10:29:58
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final UserMapper userMapper;
    public static final String SALT = "vv";

    @Override
    public long UserRegister(String userAccount, String userPassword,
                             String checkPassword, String planetCode) {
        if (StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword)
                || StringUtils.isEmpty(checkPassword) || StringUtils.isEmpty(planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空！");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不同！");
        }
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码的长度过短！");
        }

        // 不能包含特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……\n" +
                "&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matches = Pattern.compile(regEx).matcher(userAccount);
        if (matches.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能使用特殊符号！");
        }

        // 用户账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("userAccount", userAccount);
        int count = this.count(queryWrapper);
        if (count != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此账号已经被注册，请更换其他账号！");
        }

        // 对星球编号进行查重
        queryWrapper = new QueryWrapper<User>()
                .eq("planetCode", planetCode);
        count = this.count(queryWrapper);
        if (count != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号已经被注册！");
        }

        // 对密码进行加密
        String verifyPassword = md5Hex(userPassword + SALT);

        // 将数据插入到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(verifyPassword);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.NO_REGISTER, "注册失败，请重试。");
        }
        return user.getId();
    }

    @Override
    public User UserLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空！");
        }
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码长度不符合要求！");
        }

        // 不能包含特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……\n" +
                "&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matches = Pattern.compile(regEx).matcher(userAccount);
        if (matches.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能使用特殊符号！");
        }

        // 对密码进行md5加密
        String verifyPassword = md5Hex(userPassword + SALT);

        // 向数据库查找对应的信息。
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("userAccount", userAccount)
                .eq("userPassword", verifyPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User handledUser = safetyUser(user);

        request.getSession().setAttribute("User_Login_State", handledUser);

        return handledUser;
    }


    /**
     * 用户脱敏
     *
     * @param user 待脱敏的用户
     * @return 处理后的用户
     */
    @Override
    public User safetyUser(User user) {
        if (user == null) {
            return null;
        }
        User handledUser = new User();
        handledUser.setId(user.getId());
        handledUser.setUsername(user.getUsername());
        handledUser.setUserAccount(user.getUserAccount());
        handledUser.setAvatarUrl(user.getAvatarUrl());
        handledUser.setGender(user.getGender());
        handledUser.setPhone(user.getPhone());
        handledUser.setEmail(user.getEmail());
        handledUser.setPlanetCode(user.getPlanetCode());
        handledUser.setUserStatus(user.getUserStatus());
        handledUser.setTags(user.getTags());
        handledUser.setCreateTime(user.getCreateTime());
        handledUser.setUserRole(user.getUserRole());
        handledUser.setProfile(user.getProfile());
        return handledUser;
    }

    @Override
    public int UserLogout(HttpServletRequest request) {
        request.getSession().removeAttribute("User_Login_State");
        return 1;
    }

    /**
     * 使用内存查询的方法，查询标签对应的用户。
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> SearchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 第二种方法，内存查询，个人认为是先把所有的数据读入内存，然后在进行筛选
        // 即先试用空条件把所有数据查出来，再进行筛选
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(userQueryWrapper);
        Gson gson = new Gson();
        return users.stream().filter(user -> {
            String userTags = user.getTags();
            if (StringUtils.isEmpty(userTags)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(userTags, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String eachTag : tagNameList) {
                if (!tempTagNameSet.contains(eachTag)) {
                    return false;
                }
            }
            return true;
        }).map(this::safetyUser).collect(Collectors.toList());
    }

    @Override
    public int userUpdate(User user, User loginUser) {
        if (user.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 仅支持管理员和自己修改个人信息。
        boolean result = loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
        if (!result && user.getId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }


        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object user = request.getSession().getAttribute("User_Login_State");
        User loginUser = (User) user;
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        return loginUser;
    }

    /**
     * 使用简单SQL查询来获取用户
     * 下面注解是过时的意思
     *
     * @param tagNameList 传入的标签名
     * @return
     */
    @Deprecated
    public List<User> SearchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 第一种方法，SQL查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            userQueryWrapper = userQueryWrapper.like("tags", tagName);
        }
        List<User> users = userMapper.selectList(userQueryWrapper);

        return users.stream().map(this::safetyUser).collect(Collectors.toList());
    }

    public Boolean isAdmin(HttpServletRequest request) {
        Object user = request.getSession().getAttribute("User_Login_State");
        User loginUser = (User) user;
        boolean result = loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
        return result;
    }

    @Override
    public Boolean isAdmin(User user) {
        boolean result = user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
        return result;
    }
}




