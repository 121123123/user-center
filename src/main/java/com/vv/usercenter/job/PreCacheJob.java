package com.vv.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.usercenter.model.domain.User;
import com.vv.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Lang;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预热缓存
 *
 * @author vv先森
 * @create 2024-07-19 16:23
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private List<Long> mainUser = Arrays.asList(2L);
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 每天执行一次加载，预热用户
     */
    @Scheduled(cron = "0 50 16 * * *")
    public void doPreCacheJob() {
        RLock lock = redissonClient.getLock("partner:preCacheJob:doCache:lock");
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                for (Long userId : mainUser) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
                    Page<User> userPage = userService.page(new Page<User>(1, 20), queryWrapper);
                    String userKey = String.format("partner:user:recommend:%s", userId);
                    try {
                        redisTemplate.opsForValue().set(userKey, userPage, 35000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doPreCacheJob error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


    }
}
