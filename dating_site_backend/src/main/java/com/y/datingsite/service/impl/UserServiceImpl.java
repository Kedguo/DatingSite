package com.y.datingsite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.y.datingsite.Utils.AlgorithmUtils;
import com.y.datingsite.common.ErrorCode;
import com.y.datingsite.exception.BusinessException;
import com.y.datingsite.mapper.UserMapper;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.service.UserService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.y.datingsite.contant.UserConstant.ADMIN_ROLE;
import static com.y.datingsite.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author 行者
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-04-17 11:33:51
*/

@Slf4j
@Service

public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "yupi";

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //判断为空
        //todo 修改为自定义异常
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //账户长度要大于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        //密码要大于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //校验编号在1-5之间
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号校验过长");
        }

        //校验编号是否相同
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        long size = userMapper.selectCount(queryWrapper);
        if (size > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }

        //校验账户名称是否相同
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //账户不能包含特殊字符
        String regx = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regx).matcher(userAccount);
        boolean ma = matcher.find();
        if (!ma) {
            return -1;
        }

        //密码与确认密码不一致
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        //
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();

    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验

        //判断账号输入和密码是否为空
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            return null;
        }
        //账户长度要大于4
        if (userAccount.length() < 4) {
            return null;
        }
        //密码要大于8
        if (userPassword.length() < 8) {
            return null;
        }

        //账户不能包含特殊字符
        String regx = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regx).matcher(userAccount);
        boolean ma = matcher.find();
        if (!ma) {
            return null;
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //校验账户名称和密码是否相同
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null) {
            log.info("user login failed,userAccount cannot match userPassword");
            return null;
        }

        //3.用户脱敏,防止数据库中的字段泄露给前端
        User safetyUser = getSafeUser(user);


        //4.记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        //5.返回的安全的脱敏的信息
        return getSafeUser(user);

    }

    /**
     * 用户脱敏
     *
     * @param user
     * @return
     */
    @Override
    public User getSafeUser(User user) {
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserName(user.getUserName());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogOut(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 跟据标签搜索用户
     * sql查询 (该项目中未使用)
     * @param tagNameList 用户要搜索的标签
     * @return
     */
    @Deprecated //标记该方法已过时，不会执行
    public List<User> SearchUserByTagsSQL(List<String> tagNameList) {

        if (CollectionUtils.isEmpty(tagNameList)) {//判断是否为空
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long startTime = System.currentTimeMillis();
        QueryWrapper<User> querywrapper = new QueryWrapper<>();//遍历数据库

        for (String tagList : tagNameList) {//从集合中取出一个元素进行比较
            querywrapper = querywrapper.like("tags", tagList);//从数据库拿tag列的数据进行比较 #tagList 表示想搜索的值
        }
        List<User> userlist = userMapper.selectList(querywrapper);
        log.info("sql query time =" + (System.currentTimeMillis() - startTime));
        return userlist.stream().map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户
     * 内存查询
     * @param tagNameList 用户要搜索的标签
     * @return
     */
    @Override
    public List<User> SearchUserByTags (List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //先查询所有用户
        QueryWrapper<User> querywrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(querywrapper);
        Gson gson = new Gson();//
        //判断内存中是否包含要求标签
        return userList.stream().filter(user -> {//过滤返回 false 的用户，返回 true 会保留
            String tagStr = user.getTags();

            //用于将 JSON 字符串转换为 Java 对象|参数：JSON格式的字符串， 要将JSON转换为目标类型
            Set<String> temptagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
            //用于将可能 temptagNameSet为空 的值包装在一个Optional对象中
            temptagNameSet = Optional.ofNullable(temptagNameSet).orElse(new HashSet<>());//
            for (String tagName: tagNameList) {//取出传来的标签
                if(!temptagNameSet.contains(tagName)){//判断 temptagNameSet 的集合 有没有包含 用户传来的tagName
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());
    }


    /**
     * 作为校验用户是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        //校验，仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        // 如果用户为空或者用户角色不是管理员，则返回false
        if(user == null || user.getUserRole() != ADMIN_ROLE){
            return false;
        }
        //todo
        return true;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        if(loginUser == null || loginUser.getUserRole() != ADMIN_ROLE){
            return false;
        }
        return true;
    }

    /**
     * 从HTTP请求中获取当前登录的用户。
     * 该方法尝试从提供的HttpServletRequest中获取当前登录用户的信息。
     * 它检查请求的会话中是否存在用户对象，如果不存在则抛出认证异常。
     * @param request 当前HTTP请求对象，用于访问会话信息。
     * @return 如果存在有效的会话并且会话中包含用户信息，则返回对应的User对象。
     * @throws BusinessException 如果请求为null或会话中没有用户信息，抛出没有认证的异常。
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw  new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User)userObj;
    }

    /**
     * 更新用户信息。
     * 该方法用于根据提供的用户对象更新用户数据。
     * 它首先检查用户ID是否有效，然后根据用户的权限（管理员或非管理员）
     * 决定是否可以更新所请求的用户数据。
     * @param user 封装了要更新的用户信息的User对象。
     * @param loginUser 当前登录的User对象，用于进行权限验证。
     * @return 更新操作影响的数据库记录数。通常返回1表示更新成功，返回0表示未进行更新。
     * @throws BusinessException 如果用户ID无效、登录用户无权更新其他用户信息、或指定ID的用户不存在时抛出。
     */
    @Override
    public int updateUser(User user, User loginUser) {

        long userId = user.getId();
        if (userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）用户
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_AUTN);
        }
        //通过用户的ID拿到用户的信息
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //根据用户ID更新用户信息
        return userMapper.updateById(user);
    }

    /**
     * 匹配用户方法。
     * 根据当前用户的标签找出数据库中标签相似的用户。
     * 这个方法首先过滤出所有有标签的用户，然后使用编辑距离算法（Levenshtein distance）
     * 计算标签相似度，最后返回相似度最高的用户列表。
     * @param num 需要返回的用户数量，此数值应大于0且不超过20。
     * @param loginUser 当前登录的用户对象，用于从中提取标签进行比较。
     * @return 返回一个列表，包含与当前用户标签最相似的其他用户。
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        // 初始化查询条件，确保用户的标签不为空
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags"); // 确保查询的用户有标签信息
        queryWrapper.select("id", "tags"); // 仅选择id和tags字段进行查询，以减少数据量
        // 获取数据库中所有带标签的用户列表
        List<User> userList = this.list(queryWrapper);
        // 将当前用户的标签从JSON字符串转换为List
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>(){}.getType());
        // 准备用于存储用户与距离信息的列表
        List<Pair<User, Long>> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 排除空标签以及当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            // 计算标签列表之间的编辑距离
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离进行排序并获取距离最小的前num个用户
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted(Comparator.comparingLong(Pair::getValue))  // 根据配对中的值（距离）进行排序
                .limit(num)  // 限制结果数量，只取距离最小的前num个用户
                .collect(Collectors.toList());  // 收集最终的配对列表
        // 提取最匹配的用户ID列表
        List<Long> userListVo = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())  // 从配对中提取用户ID
                .collect(Collectors.toList());
        // 根据ID重新查询用户信息，并进行脱敏处理
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userListVo);  // 使用用户ID过滤查询
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafeUser(user))  // 对用户信息进行脱敏处理
                .collect(Collectors.groupingBy(User::getId));  // 按用户ID进行分组
        // 组装最终的用户列表
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userListVo) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));  // 从映射中获取用户并添加到最终列表
        }
        return finalUserList;
    }
}



