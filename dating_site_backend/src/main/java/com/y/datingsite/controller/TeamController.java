package com.y.datingsite.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y.datingsite.common.BaseResponse;
import com.y.datingsite.common.DeleteRequest;
import com.y.datingsite.common.ErrorCode;
import com.y.datingsite.common.ResultUtils;
import com.y.datingsite.exception.BusinessException;
import com.y.datingsite.model.domain.Team;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.model.domain.UserTeam;
import com.y.datingsite.model.dto.TeamQuery;
import com.y.datingsite.model.request.TeamAddRequest;
import com.y.datingsite.model.request.TeamJoinRequest;
import com.y.datingsite.model.request.TeamQuitRequest;
import com.y.datingsite.model.request.TeamUpdateRequest;
import com.y.datingsite.model.vo.TeamUserVO;
import com.y.datingsite.service.TeamService;
import com.y.datingsite.service.UserService;
import com.y.datingsite.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 队伍接口
 * @author : Yuan
 * @date :2024/4/19
 */
@RequestMapping("/team")
@RestController   //适用于编写 restful 风格的 api，返回值默认为 json 类型
@CrossOrigin(origins = { "http://localhost:5173/"}, allowCredentials = "true")  //支持所有的域名跨域
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;
    /**
     * 添加队伍
     * @param teamAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        //批量执行插入
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);

    }

    /**
     * 更新队伍接口
     * @param teamUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //更新
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     *根据id获取队伍信息
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeam( long id){
        if(id < 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);

    }

    /**
     * 查询队伍列表接口。
     * 根据用户提供的查询条件，查询并返回符合条件的队伍列表，并标注当前登录用户是否已加入这些队伍。
     * @param teamQuery 查询条件。
     * @param request HTTP请求对象，用于获取当前请求的用户信息。
     * @return 包含查询结果和加入状态的统一响应结构。
     * @throws BusinessException 当请求参数异常或查询过程中出现问题时抛出。
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        // 参数校验，确保查询条件不为空
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 确定当前用户是否拥有管理员权限
        boolean isAdmin = userService.isAdmin(request);
        // 1.获取队伍列表，考虑用户是否为管理员
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 提取查询结果中所有队伍的ID
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 2.判断当前用户是否已加入这些队伍，查询相关联的UserTeam信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 构建已加入队伍的ID集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            // 更新队伍列表，标注当前用户是否已加入各队伍
            teamList.forEach(team -> team.setHasJoin(hasJoinTeamIdSet.contains(team.getId())));
        } catch (Exception e) {
            // 异常处理逻辑，记录日志或其他处理
        }
        // 3.根据队伍ID的UserTeam记录，用于计算每个队伍的成员数量
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamJoinList = userTeamService.list(userTeamJoinQueryWrapper);
        // 将队伍成员按队伍ID分组，便于计算每个队伍的成员数量
        Map<Long, List<UserTeam>> teamIdUserTeamMap = userTeamJoinList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            // 设置每个队伍的成员数量
            team.setHasJoinNum(teamIdUserTeamMap.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        // 返回最终的队伍列表，包含成员加入情况
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍列表接口
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPages(TeamQuery teamQuery){
        if(teamQuery ==  null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //分页查询
        Team team = new Team();
        //将传来数据
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper(team);
        //执行分页查询操作
        Page resultPage = teamService.page(page, queryWrapper);
        if(resultPage == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入失败");
        }
        return ResultUtils.success(resultPage);
    }

    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户未登录");
        }
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"加入队伍失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户退出队伍的接口
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode   .NULL_ERROR,"用户未登录");
        }
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除队伍接口
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    //todo
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        //检查ID的合法性
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //根据当前登录的用户信息
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        //执行删除操作
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        //返回操作结果
        return ResultUtils.success(result);
    }

    /**
     * 查看自己创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        //查询
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 查看我加入的队伍信息
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //取出不重复队伍的id
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().
                collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        //查询
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }
}
