package com.agritrace.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String signature;
    private String realName;
    private String phone;
    private String avatar;
}
