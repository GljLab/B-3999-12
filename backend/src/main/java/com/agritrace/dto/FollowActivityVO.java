package com.agritrace.dto;

import com.agritrace.entity.FollowActivity;
import com.agritrace.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowActivityVO {
    private Long id;
    private String activityType;
    private Long targetId;
    private String extraData;
    private LocalDateTime createdAt;
    private UserVO user;

    public static FollowActivityVO from(FollowActivity activity, User user) {
        FollowActivityVO vo = new FollowActivityVO();
        vo.setId(activity.getId());
        vo.setActivityType(activity.getActivityType());
        vo.setTargetId(activity.getTargetId());
        vo.setExtraData(activity.getExtraData());
        vo.setCreatedAt(activity.getCreatedAt());
        vo.setUser(UserVO.from(user));
        return vo;
    }
}
