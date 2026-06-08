package com.agritrace.dto;

import com.agritrace.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String signature;
    private String avatar;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private Long followerCount;
    private Long followingCount;
    private Long mutualFollowCount;
    private Boolean isFollowed;
    private Boolean isMutual;

    public static UserVO from(User user) {
        return from(user, null, null, null);
    }

    public static UserVO from(User user, Long followerCount, Long followingCount, Long mutualCount) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        vo.setRole(user.getRole());
        vo.setSignature(user.getSignature());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setLastActiveAt(user.getLastActiveAt());
        vo.setFollowerCount(followerCount != null ? followerCount : 0L);
        vo.setFollowingCount(followingCount != null ? followingCount : 0L);
        vo.setMutualFollowCount(mutualCount != null ? mutualCount : 0L);
        vo.setIsFollowed(false);
        vo.setIsMutual(false);
        return vo;
    }
}
