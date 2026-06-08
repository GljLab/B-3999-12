package com.agritrace.dto;

import lombok.Data;

@Data
public class CertificateShareRequest {
    private Long certificateId;
    private String shareType;
    private String postTitle;
    private String postDescription;
}
