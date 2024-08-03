package com.vv.usercenter.service;

import com.vv.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

import java.util.ArrayList;

import static cn.hutool.crypto.digest.DigestUtil.md5Hex;

/**
 * @author vv先森
 * @create 2024-07-19 11:16
 */
@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserService userService;

    /**
     * 利用MP提供的方法分组进行插入
     */
    @Test
    public void testSALT() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ArrayList<User> users = new ArrayList<>();
        final int InsertNum = 300000;

        for (int i = 0; i < InsertNum; i++) {
            User user = new User();
            user.setUsername("测试");
            user.setUserAccount("123");
            user.setAvatarUrl("https://img1.baidu.com/it/u=1966616150,2146512490&fm=253&fmt=auto&app=138&f=JPE");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("13asda");
            user.setEmail("adfad");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("123");
            users.add(user);
        }

        userService.saveBatch(users, 5000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发插入数据
     */
    @Test
    public void testInsertUsers() {

    }

}
