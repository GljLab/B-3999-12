package com.agritrace.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class UserProfileVO {
    private UserVO user;
    private Map<String, Object> stats;
    private Map<String, Object> roleStats;
    private Long daysJoined;
    private Boolean isOwner;

    public static UserProfileVO from(UserVO user, Map<String, Object> stats,
                                     Map<String, Object> roleStats, long daysJoined, boolean isOwner) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUser(user);
        vo.setStats(stats);
        vo.setRoleStats(roleStats);
        vo.setDaysJoined(daysJoined);
        vo.setIsOwner(isOwner);
        return vo;
    }
}
