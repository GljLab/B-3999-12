package com.agritrace.dto;

import lombok.Data;
import java.util.List;

@Data
public class CertificateGenerateRequest {
    private Long traceCodeId;
    private String templateType;
    private List<BatchCertificateItem> batchItems;

    @Data
    public static class BatchCertificateItem {
        private Long traceCodeId;
        private String templateType;
    }
}
