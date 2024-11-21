package com.y.datingsite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.y.datingsite.common.ErrorCode;
import com.y.datingsite.exception.BusinessException;
import com.y.datingsite.mapper.TeamMapper;
import com.y.datingsite.model.domain.Team;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.model.domain.UserTeam;
import com.y.datingsite.model.dto.TeamQuery;
import com.y.datingsite.model.enums.TeamStatusEnum;
import com.y.datingsite.model.request.TeamJoinRequest;
import com.y.datingsite.model.request.TeamQuitRequest;
import com.y.datingsite.model.request.TeamUpdateRequest;
import com.y.datingsite.model.vo.TeamUserVO;
import com.y.datingsite.model.vo.UserVO;
import com.y.datingsite.service.TeamService;
import com.y.datingsite.service.UserService;
import com.y.datingsite.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author 行者
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-08-12 10:48:46
*/

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_AUTN);
        }
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"组队人数不符合要求");
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题字数不能超过20");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述字数过多");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(status < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不符合要求");
        }

        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum) ){
            if(StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过长");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(expireTime != null && new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"时间过期");
        }
        final long userId = loginUser.getId();
        //  g. 校验用户最多创建 5 个队伍
        //todo
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum  = this.count(queryWrapper);
        if(hasTeamNum >= 5 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍创建过多");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }

        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        //!result || teamId == null
        if(!result ){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    /**
     * 查询队伍列表。
     * 根据指定的查询条件和用户权限，从数据库中获取符合条件的队伍列表。
     * @param teamQuery 查询条件对象，包含了筛选队伍的各种条件。
     * @param isAdmin   当前用户是否为管理员，用于权限判断。
     * @return 包含查询结果的队伍列表。
     * @throws BusinessException 当查询参数异常或权限不足时抛出业务异常。
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        // 初始化查询条件构造器
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 如果提供了查询条件，构建查询条件
        if(teamQuery != null){
            // 根据 ID 精确查询
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id",id);
            }

            // 根据多个 ID 查询
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            // 根据搜索文本模糊查询队伍名称或描述
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名称模糊查询
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            // 根据描述模糊查询
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            // 根据最大人数精确查询
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum",maxNum);
            }
            // 根据用户 ID 精确查询
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("userId",userId);
            }
            // 根据队伍状态进行查询，使用枚举类型确保数据的有效性
            Integer status = teamQuery.getStatus();
            TeamStatusEnum stautsEnum = TeamStatusEnum.getEnumByValue(status);
            if(stautsEnum == null){
                stautsEnum = TeamStatusEnum.PUBLIC;//默认为公开状态
            }
            if (!isAdmin && stautsEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NOT_AUTN,"非管理员禁止查看");
            }
            queryWrapper.eq("status",stautsEnum.getValue());
        }

        // 排除过期的队伍
        // where expireTime > CURRENT_DATE or expireTime is null
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        // 执行查询并获取队伍列表
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();// 如果没有查询到任何队伍，返回空列表
        }
        // 关联查询创建人的用户信息并构建返回列表
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team: teamList) {
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;

    }

    /**
     *更改队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        // 检查队伍对象是否为null
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 获取当前队伍的ID，并验证其有效性
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取旧的队伍信息
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        // 只有管理员或队伍的创建者可以进行更新 【真真才抛异常】
        if( oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTN,"操作权限不足");
        }
        // 如果队伍状态为加密，检查是否提供了密码
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(enumByValue.equals(TeamStatusEnum.SECRET) ){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }
        // 创建新的队伍对象并复制属性，准备更新
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);

        // 执行更新操作，并返回更新结果
        return this.updateById(updateTeam);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        // 确认请求数据的完整性
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 验证队伍ID有效性,获取队伍信息，验证队伍存在
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);


        // 检查队伍是否已过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查队伍加入权限，私有队伍不允许加入
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        // 对于加密队伍，验证密码
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 检查用户已加入的队伍数量是否超过限制
        long userId = loginUser.getId();
        //实现分布式锁
        RLock lock = redissonClient.getLock("y:join_team");
        try {
            while (true) {

                //抢到锁再执行
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock:" + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinTeam >= 5) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "最多创建和加入5个队伍");
                    }
                    // 检查是否已加入该队伍，防止重复加入
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId", teamId);
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasUserjoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserjoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经加入该队伍");
                    }
                    // 检查队伍是否已满员
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 执行加入操作
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            //释放锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

        /**
     * 用户退出接口
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {

        // 参数校验：确保请求对象不为空
        if(teamQuitRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  获取并校验队伍ID的有效性
        Long teamId = teamQuitRequest.getTeamId();
        Team team = this.getTeamById(teamId);//获取到team队伍信息

        // 构建查询条件，检查用户是否为队伍成员
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("teamId",teamId);
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        // 获取队伍当前的成员数
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if(teamHasJoinNum == 1){
            // 只剩一人，队伍解散
            this.removeById(teamId);
        }else{
            // 如果队伍有多于1人
            if(team.getUserId() == userId){
                // 如果当前用户是队长，需要转移队长职位
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                // 确保有足够的用户列表进行队长转移
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                //获取新的队长id
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setUserId(nextTeamLeaderId);
                updateTeam.setId(teamId);
                // 更新队伍信息，设置新的队长
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除用户和队伍的关联关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 解散队伍接口
     * @param id
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(Long id, User loginUser) {
        // 检查队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        // 校验你是不是队伍的队长
        long userId = loginUser.getId();
        if(userId != team.getUserId()){
            throw new BusinessException(ErrorCode.NOT_AUTN,"无权限删除队伍");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }
    
    /**
     * 根据队伍ID获取该队伍的当前成员数量。
     * 此方法通过查询特定队伍ID的所有关联用户记录来计算队伍当前的人数。
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据队伍id获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId){
        // 校验队伍ID的有效性
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.NULL_ERROR, "无效的队伍ID");
        }
        // 获取队伍信息，确认队伍存在
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}




