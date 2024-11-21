package once;

import com.y.datingsite.mapper.UserMapper;
import com.y.datingsite.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 循环插入用户
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("假用户");
            user.setUserAccount("测试1");
            user.setAvatarUrl("https://s1.imagehub.cc/images/2024/08/07/4c0cf0806f5703948458963222ef4496.jpg");
            user.setProfile("大家好");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("jiayonghu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("931");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println( stopWatch.getLastTaskTimeMillis());

    }
}
