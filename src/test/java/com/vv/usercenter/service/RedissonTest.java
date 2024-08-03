package com.vv.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author vv先森
 * @create 2024-07-19 22:48
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){

        RList<Object> rList = redissonClient.getList("testRedisson");
        rList.add("0");
        System.out.println(rList.get(0));

    }
}
