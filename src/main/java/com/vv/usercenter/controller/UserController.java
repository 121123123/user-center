package com.vv.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.usercenter.common.BaseResponse;
import com.vv.usercenter.common.ErrorCode;
import com.vv.usercenter.contant.UserConstant;
import com.vv.usercenter.exception.BusinessException;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.model.request.UserLoginRequest;
import com.vv.usercenter.model.request.UserRegisterRequest;
import com.vv.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Lang;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author vv先森
 * @create 2024-07-07 16:17
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        User user = userService.UserLogin(userAccount, userPassword, request);
        return new BaseResponse<>(0, user, "ok");
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> UserLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int result = userService.UserLogout(request);
        return new BaseResponse<>(0, result, "ok");
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // return ResultUtil.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        long result = userService.UserRegister(userAccount, userPassword, checkPassword, planetCode);
        return new BaseResponse<>(0, result, "ok");
    }

    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userLoginState = session.getAttribute("User_Login_State");
        if (userLoginState == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        User loginUser = (User) userLoginState;
        User user = userService.getById(loginUser.getId());
        User safetiedUser = userService.safetyUser(user);
        return new BaseResponse<>(0, safetiedUser, "ok");
    }

    // @GetMapping("/search")
    // public BaseResponse<List<User>> search(HttpServletRequest request, String username) {
    //     if (!isAdmin(request)) {
    //         throw new BusinessException(ErrorCode.NO_AUTH);
    //     }
    //     QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
    //     if (StringUtils.isNotBlank(username)) {
    //         queryWrapper.like("username", username);
    //     }
    //     List<User> users = userService.list(queryWrapper);
    //     List<User> userList = users.stream().map(user -> userService.safetyUser(user)).collect(Collectors.toList());
    //     return new BaseResponse<>(0, userList, "ok");
    // }

    @GetMapping("/search")
    public BaseResponse<List<User>> search(HttpServletRequest request, @ModelAttribute User requestUser) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        String username = requestUser.getUsername();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        String userAccount = requestUser.getUserAccount();
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        Integer gender = requestUser.getGender();
        if (gender != null) {
            queryWrapper.eq("gender", gender);
        }
        String phone = requestUser.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            queryWrapper.eq("phone", phone);
        }
        String email = requestUser.getEmail();
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.eq("email", email);
        }
        Integer userStatus = requestUser.getUserStatus();
        if (userStatus != null) {
            queryWrapper.eq("userStatus", userStatus);
        }
        String planetCode = requestUser.getPlanetCode();
        if (StringUtils.isNotBlank(planetCode)) {
            queryWrapper.eq("planetCode", planetCode);
        }
        Integer userRole = requestUser.getUserRole();
        if (userRole != null) {
            queryWrapper.eq("userRole", userRole);
        }

        List<User> users = userService.list(queryWrapper);
        List<User> userList = users.stream().map(user -> userService.safetyUser(user)).collect(Collectors.toList());
        return new BaseResponse<>(0, userList, "ok");
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> getUserByTags(@RequestParam(required = false) List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.SearchUsersByTags(tags);
        return new BaseResponse<>(0, users, "ok");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(HttpServletRequest request, @RequestBody long id) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.removeById(id);
        return new BaseResponse<>(0, true, "ok");
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(HttpServletRequest request, @RequestBody User requestUser) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (requestUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("userAccount", requestUser.getUserAccount());
        boolean update = userService.update(requestUser, userUpdateWrapper);
        if (!update) {
            throw new BusinessException(ErrorCode.Fail_UPDATE);
        }
        return new BaseResponse<>(0, true, "ok");
    }

    @PostMapping("/info/update")
    public BaseResponse<Integer> userInfoUpdate(HttpServletRequest request, @RequestBody User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.userUpdate(user, loginUser);
        return new BaseResponse<>(0, result, "ok");
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userKey = String.format("partner:user:recommend:%s", loginUser.getId());
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(userKey);
        if (userPage != null) {
            return new BaseResponse<>(0, userPage, "ok");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        userPage = userService.page(new Page<User>(pageNum, pageSize), queryWrapper);
        try {
            redisTemplate.opsForValue().set(userKey, userPage, 35000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set error");
        }
        return new BaseResponse<>(0, userPage, "ok");
    }

    @DeleteMapping
    public BaseResponse<Boolean> deleteUser(HttpServletRequest request, @RequestBody String userAccount) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().eq("userAccount", userAccount);
        boolean remove = userService.remove(queryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.Fail_DELETE);
        }
        return new BaseResponse<>(0, true, "ok");
    }

    public Boolean isAdmin(HttpServletRequest request) {
        Object user = request.getSession().getAttribute("User_Login_State");
        User loginUser = (User) user;
        boolean result = loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
        return result;
    }
}
