package com.vv.usercenter.service;

import com.vv.usercenter.model.domain.User;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.hutool.crypto.digest.DigestUtil.md5Hex;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author vv先森
 * @create 2024-07-07 10:37
 * 用户服务测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testSALT(){
        String password = "qwerf";
        String verifyPassword = md5Hex(password);
        System.out.println(verifyPassword);
    }

    @Test
    public void testAddUser2(){
        String username = "1234afng";
        String regEx="[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……\n" +
                "&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matches = Pattern.compile(regEx).matcher(username);
        System.out.println(matches.find());
    }

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("asdf");
        user.setUserAccount("123");
        user.setAvatarUrl("afadf");
        user.setGender(0);
        user.setUserPassword("asdf");
        user.setPhone("13asda");
        user.setEmail("adfad");
        boolean save = userService.save(user);
        Assertions.assertTrue(save);
        System.out.println(user.getId());

    }

    // @Test
    // public void userRegister() {
    //     long hspStudy = userService.UserRegister("hspStudy", "123456789", "123456789",1);
    //     Assertions.assertTrue(hspStudy > 0);
    // }

    @Test
    public void searchByTags(){
        List<String> tagNames = Arrays.asList("java","c++");
        List<User> users = userService.SearchUsersByTags(tagNames);
        Assert.assertNotNull(users);
    }

}