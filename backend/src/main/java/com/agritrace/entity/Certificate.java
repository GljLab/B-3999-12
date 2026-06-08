package com.agritrace.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "certificate")
public class Certificate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(columnDefinition = "TEXT")
    private String productDescription;
    private String productImageUrl;
    private LocalDate harvestDate;
    private String farmerName;
    private String farmPhotoUrl;
    @Column(columnDefinition = "TEXT")
    private String brandIntro;
    private String brandLogoUrl;
    private String batchNo;
    private LocalDate productionDate;
    private String qualityGrade;
    @Column(columnDefinition = "TEXT")
    private String logisticsSummary;
    private String traceCode;
    private String digitalSignature;
    private Integer viewCount;
    private Integer shareCount;
    private Integer verifyCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (viewCount == null) viewCount = 0;
        if (shareCount == null) shareCount = 0;
        if (verifyCount == null) verifyCount = 0;
        if (status == null) status = 1;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
