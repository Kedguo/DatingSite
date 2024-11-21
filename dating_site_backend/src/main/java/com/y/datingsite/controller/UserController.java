package com.y.datingsite.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y.datingsite.common.BaseResponse;
import com.y.datingsite.common.ErrorCode;
import com.y.datingsite.common.ResultUtils;
import com.y.datingsite.exception.BusinessException;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.model.request.UserLoginRequest;
import com.y.datingsite.model.request.UserRegisterRequest;
import com.y.datingsite.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.y.datingsite.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author : Yuan
 * @date :2024/4/19
 */
@RequestMapping("/user")
@RestController   //适用于编写 restful 风格的 api，返回值默认为 json 类型
@CrossOrigin(origins = { "http://localhost:5173/"}, allowCredentials = "true")  //支持所有的域名跨域
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
         if(userRegisterRequest == null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        //判断传过来的参数是否为空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            return null;
        }
        Long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //判断传过来的参数是否为空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount,userPassword,request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogOut(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogOut(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户的登录态、信息接口
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){

        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //TODO 校验用户是否合法
        User user = userService.getById(currentUser.getId());
        //返回脱敏后的用户信息
        User safeUser = userService.getSafeUser(user);
        return ResultUtils.success(safeUser);
    }


    /**
     * 查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        //校验
        if(! userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTN);
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            userQueryWrapper.like("username",username);
        }
        //从数据库中检索所有用户并返回它们作为一个列表。
        List<User> userList = userService.list(userQueryWrapper);
        List<User> list = userList.stream().map(user -> {
            return userService.getSafeUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 根据标签搜索数据
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.SearchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody long id,HttpServletRequest request){

        //校验
        if(! userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        if(id <= 0){
            return null;
        }
        //根据用户ID从数据库中删除一个用户记录
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新用户信息的接口。
     * 此接口接收一个JSON格式的用户对象，并对其进行更新。更新操作依赖于当前登录的用户权限。
     * 只有具有相应权限的用户（例如管理员）可以更新其他用户的信息，普通用户只能更新自己的信息。
     * @param user 从请求体中获取的User对象，包含需要更新的数据。
     * @param request HTTP请求对象，用于获取当前会话中的用户信息。
     * @return 返回一个包含操作结果的BaseResponse对象，操作成功则返回更新影响的记录数。
     * @throws BusinessException 如果输入参数有误或登录用户无权更新指定用户信息时抛出。
     * @throws RequestBody 前端传来 JSON 数据格式才生效，前提是 post 请求方式
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        // 1.校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // TODO:补充校验，如果用户没有传任何要更新的值，就直接报错，不执行 update 语句

        // 2.鉴权：验证当前请求中的用户是否具有更新所提供用户数据的权限。
        User loginUser = userService.getLoginUser(request);
        // 3.触发更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     *推荐主页
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> RecommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("datingsite:user:recommend:%s",loginUser.getId());
        ValueOperations<String, Object> ValueOperations = redisTemplate.opsForValue();
        //有缓存，直接读缓存
        Page<User> userPage = (Page<User>) ValueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }
        //如果没有缓存，从数据库查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //从数据库中检索所有用户并返回它们作为一个列表。
        Page<User> userList = userService.page(new Page<>(pageNum,pageSize),userQueryWrapper);
        //写缓存
        try {
            // 查询结果存入Redis缓存，并设置有效期为20000毫秒（20秒），以控制数据的新鲜度。
            // 异常处理用于捕获并记录设置缓存时可能出现的Redis操作错误。
            ValueOperations.set(redisKey,userPage,20000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userList);
    }
    /**
     * 匹配用户接口。
     * 此接口用于匹配当前登录用户与其他用户，基于某种算法（例如相似标签、兴趣或活动）来找出最匹配的用户列表。
     * @param num 请求匹配的用户数量，此数量必须在1到20之间。
     * @param request HTTP请求对象，用于获取当前请求的用户会话信息。
     * @return 返回符合匹配条件的用户列表的统一响应结构。
     * @throws BusinessException 如果请求参数num无效或匹配过程中发生错误，则抛出业务异常。
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        // 校验请求的匹配用户数量是否在允许的范围内
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求匹配用户数量必须在1到20之间");
        }
        // 从会话中获取当前登录的用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用userService的matchUsers方法进行用户匹配，传入数量和当前用户
        List<User> matchedUsers = userService.matchUsers(num, loginUser);
        // 封装并返回匹配结果
        return ResultUtils.success(matchedUsers);
    }

}
