package com.agritrace.dto;

import com.agritrace.entity.User;
import com.agritrace.entity.UserFollow;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowUserVO {
    private Long id;
    private String realName;
    private String username;
    private String role;
    private String signature;
    private String avatar;
    private Long followerCount;
    private Long followingCount;
    private Boolean isMutual;
    private LocalDateTime followedAt;

    public static FollowUserVO from(User user, UserFollow follow, Long followerCount, Long followingCount, boolean isMutual) {
        FollowUserVO vo = new FollowUserVO();
        vo.setId(user.getId());
        vo.setRealName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setSignature(user.getSignature());
        vo.setAvatar(user.getAvatar());
        vo.setFollowerCount(followerCount != null ? followerCount : 0L);
        vo.setFollowingCount(followingCount != null ? followingCount : 0L);
        vo.setIsMutual(isMutual);
        vo.setFollowedAt(follow != null ? follow.getCreatedAt() : null);
        return vo;
    }
}
