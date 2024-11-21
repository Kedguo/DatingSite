package com.y.datingsite.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.y.datingsite.model.domain.Team;
import com.y.datingsite.model.domain.User;
import com.y.datingsite.model.dto.TeamQuery;
import com.y.datingsite.model.request.TeamJoinRequest;
import com.y.datingsite.model.request.TeamQuitRequest;
import com.y.datingsite.model.request.TeamUpdateRequest;
import com.y.datingsite.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 行者
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-08-12 10:48:46
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍数据
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);
    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @param loginUser
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出接口
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);


    /**
     * 删除接口
     * @param id
     * @return
     */
    boolean deleteTeam(Long id,User loginUser);
}
