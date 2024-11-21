package com.y.datingsite.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.y.datingsite.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author 行者
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-04-17 11:33:51
*  用户服务
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    校验编号
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param user
     * @return
     */
    User getSafeUser(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogOut(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要搜索的标签
     * @return
     */
    List<User> SearchUserByTags(List<String> tagNameList);

    /**
     * 校验用户是否为管理员
     * @param request
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取当前登陆用户的登录态
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
