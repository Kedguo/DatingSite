package com.y.datingsite.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.y.datingsite.mapper.UserTeamMapper;
import com.y.datingsite.model.domain.UserTeam;
import com.y.datingsite.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 行者
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-08-12 10:46:56
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




