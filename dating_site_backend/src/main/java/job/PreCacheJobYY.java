package job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 * @author : Yuan
 * @date :2024/8/9
 */
@Component
@Slf4j
public class PreCacheJobYY {


    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    //重点用户列表，目前包含一个用户ID为2的示例
    private List<Long> mainUserList = Arrays.asList(2L);

    /**
     * 定时任务，每天22点04分执行，为重点用户预缓存推荐列表。
     * 默认查询前20个用户并设置30秒的缓存过期时间。
     */
    //每天16时：21分：00秒执行，预热推荐用户
    @Scheduled(cron = "0 04 22 * * *")
    public void doCacheRecommendUser(){
        // 获取分布式锁，防止多个任务同时执行
        RLock lock = redissonClient.getLock("datingsite:precachejob:docache:lock");
        try {
            // 尝试立即获取锁，如果成功获取到锁，则执行下面的代码块；如果获取失败，则不进行等待，
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock: " + Thread.currentThread().getId());
                // 遍历重点用户列表，为每个用户预缓存推荐列表
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1,8),queryWrapper);
                    String  redisKey = String.format("datingsite:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,500000, TimeUnit.MILLISECONDS);
                        System.out.println("已经写入");
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("doCacheRecommendUser error",e);
        }finally {
            //释放锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }


    }


}
