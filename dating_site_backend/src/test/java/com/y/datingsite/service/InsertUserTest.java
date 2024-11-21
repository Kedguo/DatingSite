/*
package com.y.datingsite.service;
import com.y.datingsite.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

*/
/**
 * 执行用户数据的批量插入操作，使用批处理以提高性能。
 * 创建了5000个用户实例并将它们添加到列表中，最后通过批处理方法插入数据库。
 * 这种方法减少了数据库交互次数，从而显著提高了性能。
 *//*

@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserService userService;
    // 线程池的设置
    private ExecutorService executorService = new ThreadPoolExecutor(
            60, // corePoolSize: 核心线程数。线程池中始终保持活跃的线程数量，即使它们处于空闲状态。
            1000, // maximumPoolSize: 最大线程数。线程池中允许的最大线程数量。
            10000, // keepAliveTime: 当线程数超过核心线程数时，这是非核心线程空闲前的最大存活时间。
            TimeUnit.MINUTES, // 时间单位。上面的 keepAliveTime 的单位。
            new ArrayBlockingQueue<>(10000) // 工作队列。存放待执行任务的阻塞队列，具有先进先出等特性的阵列支持的有界队列。
    );

    */
/**
     * 批量插入用户
     *//*

    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();//开始计时
        final int INSERT_NUM = 5000;//定义插入的用户数量
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("假用户2");
            user.setUserAccount("ceshi2");
            user.setAvatarUrl("https://s1.imagehub.cc/images/2024/08/07/f1950ddf5e9e2fd8480dc8f43df4a4a4.jpg");
            user.setProfile("大家好");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("jiayonghu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("931");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,500);// 使用批处理每次提交500个用户
        stopWatch.stop();//停止计时
        System.out.println( "执行所需的毫秒:" + stopWatch.getLastTaskTimeMillis());
    }

    */
/**
     * 并发批量插入用户
     *//*

    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();//开始计时
        final int INSERT_NUM = 100000;//定义插入的用户数量
        int batchSize = 2500;//，每批处理的数据量
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            List<User> userList = new ArrayList<>();
            while(true){
                j++;
                User user = new User();
                user.setUserName("假用户2");
                user.setUserAccount("ceshi2");
                user.setAvatarUrl("https://s1.imagehub.cc/images/2024/08/07/f1950ddf5e9e2fd8480dc8f43df4a4a4.jpg");
                user.setProfile("大家好");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("jiayonghu-yusha@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("931");
                user.setTags("[]");
                userList.add(user);
                if(j % 10000 == 0){
                    break;
                }
            }
            //异步任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);// 使用批处理每次提交500个用户
            },executorService);
            futureList.add(future);//相当于拿到10个异步任务
        }
        //使用join是为了保证所有的异步任务都执行完成，才会执行下一行代码
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();//停止计时
        System.out.println( "执行所需的毫秒:" + stopWatch.getLastTaskTimeMillis());
    }

}
*/
