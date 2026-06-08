package com.agritrace.dto;

import com.agritrace.entity.Certificate;
import lombok.Data;

@Data
public class CertificateDetailVO {
    private Long id;
    private String certificateNo;
    private Long userId;
    private Long productId;
    private Long traceCodeId;
    private Long batchId;
    private String templateType;
    private String productName;
    private String productCategory;
    private String productOrigin;
    private String productDescription;
    private String productImageUrl;
    private String harvestDate;
    private String farmerName;
    private String farmPhotoUrl;
    private String brandIntro;
    private String brandLogoUrl;
    private String batchNo;
    private String productionDate;
    private String qualityGrade;
    private String logisticsSummary;
    private String traceCode;
    private String digitalSignature;
    private Integer viewCount;
    private Integer shareCount;
    private Integer verifyCount;
    private Integer status;
    private String createdAt;

    public static CertificateDetailVO from(Certificate cert) {
        CertificateDetailVO vo = new CertificateDetailVO();
        vo.setId(cert.getId());
        vo.setCertificateNo(cert.getCertificateNo());
        vo.setUserId(cert.getUserId());
        vo.setProductId(cert.getProductId());
        vo.setTraceCodeId(cert.getTraceCodeId());
        vo.setBatchId(cert.getBatchId());
        vo.setTemplateType(cert.getTemplateType());
        vo.setProductName(cert.getProductName());
        vo.setProductCategory(cert.getProductCategory());
        vo.setProductOrigin(cert.getProductOrigin());
        vo.setProductDescription(cert.getProductDescription());
        vo.setProductImageUrl(cert.getProductImageUrl());
        vo.setHarvestDate(cert.getHarvestDate() != null ? cert.getHarvestDate().toString() : null);
        vo.setFarmerName(cert.getFarmerName());
        vo.setFarmPhotoUrl(cert.getFarmPhotoUrl());
        vo.setBrandIntro(cert.getBrandIntro());
        vo.setBrandLogoUrl(cert.getBrandLogoUrl());
        vo.setBatchNo(cert.getBatchNo());
        vo.setProductionDate(cert.getProductionDate() != null ? cert.getProductionDate().toString() : null);
        vo.setQualityGrade(cert.getQualityGrade());
        vo.setLogisticsSummary(cert.getLogisticsSummary());
        vo.setTraceCode(cert.getTraceCode());
        vo.setDigitalSignature(cert.getDigitalSignature());
        vo.setViewCount(cert.getViewCount());
        vo.setShareCount(cert.getShareCount());
        vo.setVerifyCount(cert.getVerifyCount());
        vo.setStatus(cert.getStatus());
        vo.setCreatedAt(cert.getCreatedAt() != null ? cert.getCreatedAt().toString() : null);
        return vo;
    }
}
