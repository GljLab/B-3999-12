package com.agritrace.dto;

import com.agritrace.entity.Certificate;
import lombok.Data;

@Data
public class CertificateVO {
    private Long id;
    private String certificateNo;
    private Long productId;
    private String productName;
    private String productCategory;
    private String productOrigin;
    private String productImageUrl;
    private String farmerName;
    private String templateType;
    private String traceCode;
    private Integer viewCount;
    private Integer shareCount;
    private Integer verifyCount;
    private Integer status;
    private String createdAt;

    public static CertificateVO from(Certificate cert) {
        CertificateVO vo = new CertificateVO();
        vo.setId(cert.getId());
        vo.setCertificateNo(cert.getCertificateNo());
        vo.setProductId(cert.getProductId());
        vo.setProductName(cert.getProductName());
        vo.setProductCategory(cert.getProductCategory());
        vo.setProductOrigin(cert.getProductOrigin());
        vo.setProductImageUrl(cert.getProductImageUrl());
        vo.setFarmerName(cert.getFarmerName());
        vo.setTemplateType(cert.getTemplateType());
        vo.setTraceCode(cert.getTraceCode());
        vo.setViewCount(cert.getViewCount());
        vo.setShareCount(cert.getShareCount());
        vo.setVerifyCount(cert.getVerifyCount());
        vo.setStatus(cert.getStatus());
        vo.setCreatedAt(cert.getCreatedAt() != null ? cert.getCreatedAt().toString() : null);
        return vo;
    }
}
