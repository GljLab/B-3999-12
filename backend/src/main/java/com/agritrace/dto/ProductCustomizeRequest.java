package com.agritrace.dto;

import lombok.Data;

@Data
public class ProductCustomizeRequest {
    private Long productId;
    private String farmPhotoUrl;
    private String brandIntro;
    private String brandLogoUrl;
}
