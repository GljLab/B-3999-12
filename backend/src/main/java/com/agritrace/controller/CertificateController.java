package com.agritrace.controller;

import com.agritrace.dto.*;
import com.agritrace.entity.*;
import com.agritrace.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    @Autowired private CertificateRepository certificateRepository;
    @Autowired private CertificateShareLogRepository shareLogRepository;
    @Autowired private CertificateViewLogRepository viewLogRepository;
    @Autowired private TracingCodeRepository tracingCodeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductBatchRepository batchRepository;
    @Autowired private LogisticsRepository logisticsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommunityPostRepository communityPostRepository;

    private static final Set<String> VALID_TEMPLATES = Set.of("CLASSIC", "MINIMAL", "CHINESE", "TECH");

    private Long getCurrentUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        return uid != null ? ((Number) uid).longValue() : null;
    }

    private String generateCertificateNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "CERT-" + date + "-" + random;
    }

    private String generateDigitalSignature(Certificate cert) {
        try {
            String raw = cert.getCertificateNo() + cert.getTraceCode() + cert.getProductName() + cert.getCreatedAt();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private Certificate buildCertificateFromTraceCode(Long traceCodeId, String templateType, Long userId) {
        TracingCode tc = tracingCodeRepository.findById(traceCodeId).orElse(null);
        if (tc == null) return null;
        if (tc.getStatus() != null && tc.getStatus() == 0) return null;

        Product p = productRepository.findById(tc.getProductId()).orElse(null);
        if (p == null) return null;

        User farmer = userRepository.findById(p.getFarmerId()).orElse(null);

        ProductBatch batch = null;
        if (tc.getBatchId() != null) {
            batch = batchRepository.findById(tc.getBatchId()).orElse(null);
        }

        List<Logistics> logistics = logisticsRepository.findByTraceCodeId(tc.getId());
        String logisticsJson = buildLogisticsJson(logistics);

        Certificate cert = new Certificate();
        cert.setCertificateNo(generateCertificateNo());
        cert.setUserId(userId);
        cert.setProductId(p.getId());
        cert.setTraceCodeId(tc.getId());
        cert.setBatchId(tc.getBatchId());
        cert.setTemplateType(templateType);
        cert.setProductName(p.getProductName());
        cert.setProductCategory(p.getCategory());
        cert.setProductOrigin(p.getOrigin());
        cert.setProductDescription(p.getDescription());
        cert.setProductImageUrl(p.getImageUrl());
        cert.setHarvestDate(p.getHarvestDate());
        cert.setFarmerName(farmer != null ? (farmer.getRealName() != null ? farmer.getRealName() : farmer.getUsername()) : null);
        cert.setFarmPhotoUrl(p.getFarmPhotoUrl());
        cert.setBrandIntro(p.getBrandIntro());
        cert.setBrandLogoUrl(p.getBrandLogoUrl());
        cert.setBatchNo(batch != null ? batch.getBatchNo() : null);
        cert.setProductionDate(batch != null ? batch.getProductionDate() : null);
        cert.setQualityGrade(batch != null ? batch.getQualityGrade() : null);
        cert.setLogisticsSummary(logisticsJson);
        cert.setTraceCode(tc.getTraceCode());

        certificateRepository.save(cert);
        cert.setDigitalSignature(generateDigitalSignature(cert));
        certificateRepository.save(cert);

        return cert;
    }

    private String buildLogisticsJson(List<Logistics> logistics) {
        if (logistics == null || logistics.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < logistics.size(); i++) {
            Logistics l = logistics.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"location\":\"").append(escapeJson(l.getLocation())).append("\"")
              .append(",\"statusDesc\":\"").append(escapeJson(l.getStatusDesc())).append("\"")
              .append(",\"recordedAt\":\"").append(l.getRecordedAt() != null ? l.getRecordedAt().toString() : "").append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @PostMapping("/generate")
    @Transactional
    public Result<CertificateDetailVO> generateCertificate(HttpServletRequest request, @RequestBody CertificateGenerateRequest req) {
        if (req.getTraceCodeId() == null) {
            return Result.error(400, "溯源码ID不能为空");
        }
        String templateType = req.getTemplateType() != null ? req.getTemplateType().toUpperCase() : "CLASSIC";
        if (!VALID_TEMPLATES.contains(templateType)) {
            return Result.error(400, "无效的模板类型");
        }

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Certificate cert = buildCertificateFromTraceCode(req.getTraceCodeId(), templateType, userId);
        if (cert == null) {
            return Result.error(404, "溯源码无效或产品不存在");
        }

        return Result.success(CertificateDetailVO.from(cert));
    }

    @PostMapping("/batch-generate")
    @Transactional
    public Result<List<CertificateDetailVO>> batchGenerate(HttpServletRequest request, @RequestBody CertificateGenerateRequest req) {
        if (req.getBatchItems() == null || req.getBatchItems().isEmpty()) {
            return Result.error(400, "批量生成列表不能为空");
        }
        if (req.getBatchItems().size() > 20) {
            return Result.error(400, "单次批量生成不能超过20个");
        }

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        List<CertificateDetailVO> results = new ArrayList<>();
        for (CertificateGenerateRequest.BatchCertificateItem item : req.getBatchItems()) {
            String templateType = item.getTemplateType() != null ? item.getTemplateType().toUpperCase() : "CLASSIC";
            if (!VALID_TEMPLATES.contains(templateType)) {
                templateType = "CLASSIC";
            }
            Certificate cert = buildCertificateFromTraceCode(item.getTraceCodeId(), templateType, userId);
            if (cert != null) {
                results.add(CertificateDetailVO.from(cert));
            }
        }

        return Result.success(results);
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> myCertificates(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Certificate> certPage = certificateRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("content", certPage.getContent().stream().map(CertificateVO::from).toList());
        result.put("totalElements", certPage.getTotalElements());
        result.put("totalPages", certPage.getTotalPages());
        result.put("currentPage", certPage.getNumber());
        result.put("last", certPage.isLast());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<CertificateDetailVO> getCertificateDetail(@PathVariable Long id, HttpServletRequest request) {
        Certificate cert = certificateRepository.findById(id).orElse(null);
        if (cert == null) {
            return Result.error(404, "证书不存在");
        }

        cert.setViewCount(cert.getViewCount() + 1);
        certificateRepository.save(cert);

        CertificateViewLog viewLog = new CertificateViewLog();
        viewLog.setCertificateId(id);
        viewLog.setSource("DIRECT");
        viewLog.setViewerIp(request.getRemoteAddr());
        viewLogRepository.save(viewLog);

        return Result.success(CertificateDetailVO.from(cert));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<?> deleteCertificate(HttpServletRequest request, @PathVariable Long id) {
        Certificate cert = certificateRepository.findById(id).orElse(null);
        if (cert == null) {
            return Result.error(404, "证书不存在");
        }

        Long userId = getCurrentUserId(request);
        if (!cert.getUserId().equals(userId)) {
            return Result.error(403, "无权删除此证书");
        }

        cert.setStatus(0);
        certificateRepository.save(cert);
        return Result.success("删除成功");
    }

    @GetMapping("/verify/{certificateNo}")
    public Result<Map<String, Object>> verifyCertificate(@PathVariable String certificateNo) {
        Certificate cert = certificateRepository.findByCertificateNo(certificateNo).orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (cert == null || cert.getStatus() == 0) {
            result.put("valid", false);
            result.put("message", cert == null ? "证书不存在" : "证书已被作废");
            return Result.success(result);
        }

        cert.setVerifyCount(cert.getVerifyCount() + 1);
        certificateRepository.save(cert);

        CertificateViewLog viewLog = new CertificateViewLog();
        viewLog.setCertificateId(cert.getId());
        viewLog.setSource("VERIFY");
        viewLogRepository.save(viewLog);

        String currentSignature = generateDigitalSignature(cert);
        boolean signatureValid = currentSignature.equals(cert.getDigitalSignature());

        result.put("valid", true);
        result.put("signatureValid", signatureValid);
        result.put("certificate", CertificateDetailVO.from(cert));
        result.put("message", signatureValid ? "证书验证通过，数字签名一致" : "证书验证通过，但数字签名不一致，可能已被篡改");
        return Result.success(result);
    }

    @PostMapping("/share")
    @Transactional
    public Result<Map<String, Object>> shareCertificate(HttpServletRequest request, @RequestBody CertificateShareRequest req) {
        if (req.getCertificateId() == null) {
            return Result.error(400, "证书ID不能为空");
        }
        if (req.getShareType() == null || !Set.of("COMMUNITY", "QRCODE", "POSTER").contains(req.getShareType())) {
            return Result.error(400, "无效的分享类型");
        }

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Certificate cert = certificateRepository.findById(req.getCertificateId()).orElse(null);
        if (cert == null || cert.getStatus() == 0) {
            return Result.error(404, "证书不存在或已作废");
        }

        CertificateShareLog shareLog = new CertificateShareLog();
        shareLog.setCertificateId(req.getCertificateId());
        shareLog.setShareType(req.getShareType());
        shareLog.setShareUserId(userId);

        Map<String, Object> result = new HashMap<>();

        if ("COMMUNITY".equals(req.getShareType())) {
            String title = req.getPostTitle() != null && !req.getPostTitle().trim().isEmpty()
                    ? req.getPostTitle().trim()
                    : "我查询了「" + cert.getProductName() + "」的溯源信息";
            String description = req.getPostDescription() != null && !req.getPostDescription().trim().isEmpty()
                    ? req.getPostDescription().trim()
                    : "我查询了这个产品的溯源信息，生成了溯源证书，分享给大家！";

            CommunityPost post = new CommunityPost();
            post.setUserId(userId);
            post.setTitle(title);
            post.setDescription(description);
            communityPostRepository.save(post);

            shareLog.setPostId(post.getId());
            result.put("postId", post.getId());
        }

        shareLogRepository.save(shareLog);

        cert.setShareCount(cert.getShareCount() + 1);
        certificateRepository.save(cert);

        result.put("shareCount", cert.getShareCount());
        return Result.success(result);
    }

    @GetMapping("/product/{productId}/count")
    public Result<Map<String, Object>> getProductCertificateCount(@PathVariable Long productId) {
        long count = certificateRepository.countByProductIdAndStatus(productId, 1);
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("certificateCount", count);
        return Result.success(result);
    }

    @GetMapping("/farmer/products-stats")
    public Result<List<Map<String, Object>>> getFarmerProductStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        List<Product> products = productRepository.findByFarmerId(userId);
        List<Map<String, Object>> stats = new ArrayList<>();
        for (Product p : products) {
            long count = certificateRepository.countByProductIdAndStatus(p.getId(), 1);
            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("productName", p.getProductName());
            item.put("certificateCount", count);
            stats.add(item);
        }
        stats.sort((a, b) -> Long.compare((Long) b.get("certificateCount"), (Long) a.get("certificateCount")));
        return Result.success(stats);
    }

    @GetMapping("/farmer/certificate-users/{productId}")
    public Result<Map<String, Object>> getCertificateUsers(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId(request);
        String role = (String) request.getAttribute("role");
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return Result.error(404, "产品不存在");
        }
        if (!product.getFarmerId().equals(userId) && !"SYS_ADMIN".equals(role)) {
            return Result.error(403, "无权查看");
        }

        List<Certificate> certs = certificateRepository.findByProductIdAndStatus(productId, 1);
        List<Map<String, Object>> userList = certs.stream().map(cert -> {
            User user = userRepository.findById(cert.getUserId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("userId", cert.getUserId());
            item.put("username", user != null ? user.getUsername() : "未知");
            item.put("realName", user != null ? user.getRealName() : null);
            item.put("certificateId", cert.getId());
            item.put("certificateNo", cert.getCertificateNo());
            item.put("createdAt", cert.getCreatedAt() != null ? cert.getCreatedAt().toString() : null);
            return item;
        }).sorted((a, b) -> ((String) b.get("createdAt")).compareTo((String) a.get("createdAt"))).toList();

        int from = Math.min(page * size, userList.size());
        int to = Math.min((page + 1) * size, userList.size());
        List<Map<String, Object>> pagedList = from < to ? userList.subList(from, to) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("content", pagedList);
        result.put("totalElements", userList.size());
        result.put("totalPages", (int) Math.ceil((double) userList.size() / size));
        result.put("currentPage", page);
        return Result.success(result);
    }

    @GetMapping("/rankings/products")
    public Result<List<Map<String, Object>>> productRankings() {
        Pageable top20 = PageRequest.of(0, 20);
        List<Object[]> rows = certificateRepository.countByProductTop(top20);

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Object[] row : rows) {
            Long productId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            Product p = productRepository.findById(productId).orElse(null);
            if (p != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", productId);
                item.put("productName", p.getProductName());
                item.put("category", p.getCategory());
                item.put("origin", p.getOrigin());
                item.put("certificateCount", count);
                rankings.add(item);
            }
        }
        return Result.success(rankings);
    }

    @GetMapping("/rankings/farmers")
    public Result<List<Map<String, Object>>> farmerRankings() {
        Pageable top20 = PageRequest.of(0, 20);
        List<Object[]> rows = certificateRepository.countByFarmerTop(top20);

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Object[] row : rows) {
            Long farmerId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            User farmer = userRepository.findById(farmerId).orElse(null);
            if (farmer != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("farmerId", farmerId);
                item.put("farmerName", farmer.getRealName() != null ? farmer.getRealName() : farmer.getUsername());
                item.put("certificateCount", count);
                rankings.add(item);
            }
        }
        return Result.success(rankings);
    }

    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> adminStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"SYS_ADMIN".equals(role)) {
            return Result.error(403, "无权访问");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCertificates", certificateRepository.countByStatus(1));
        stats.put("todayCertificates", certificateRepository.countByCreatedAtBetween(todayStart, now));
        stats.put("weekCertificates", certificateRepository.countByCreatedAtBetween(sevenDaysAgo, now));
        stats.put("monthCertificates", certificateRepository.countByCreatedAtBetween(thirtyDaysAgo, now));

        List<Object[]> dailyTrend = certificateRepository.countDaily(thirtyDaysAgo);
        List<Map<String, Object>> trendList = new ArrayList<>();
        for (Object[] row : dailyTrend) {
            Map<String, Object> t = new HashMap<>();
            t.put("date", row[0].toString());
            t.put("count", ((Number) row[1]).longValue());
            trendList.add(t);
        }
        stats.put("dailyTrend", trendList);

        List<Object[]> templateStats = certificateRepository.countByTemplateType();
        List<Map<String, Object>> templateList = new ArrayList<>();
        for (Object[] row : templateStats) {
            Map<String, Object> t = new HashMap<>();
            t.put("templateType", row[0].toString());
            t.put("count", ((Number) row[1]).longValue());
            templateList.add(t);
        }
        stats.put("templateStats", templateList);

        stats.put("totalShareCount", shareLogRepository.count());
        stats.put("communityShares", shareLogRepository.countByShareType("COMMUNITY"));
        stats.put("qrcodeShares", shareLogRepository.countByShareType("QRCODE"));
        stats.put("posterShares", shareLogRepository.countByShareType("POSTER"));
        stats.put("totalViewCount", viewLogRepository.count());
        stats.put("shareViewCount", viewLogRepository.countShareViewsSince(thirtyDaysAgo));

        return Result.success(stats);
    }

    @PutMapping("/product/customize")
    @Transactional
    public Result<?> customizeProduct(HttpServletRequest request, @RequestBody ProductCustomizeRequest req) {
        if (req.getProductId() == null) {
            return Result.error(400, "产品ID不能为空");
        }

        Long userId = getCurrentUserId(request);
        String role = (String) request.getAttribute("role");

        Product product = productRepository.findById(req.getProductId()).orElse(null);
        if (product == null) {
            return Result.error(404, "产品不存在");
        }
        if (!product.getFarmerId().equals(userId) && !"SYS_ADMIN".equals(role)) {
            return Result.error(403, "无权修改");
        }

        if (req.getFarmPhotoUrl() != null) {
            product.setFarmPhotoUrl(req.getFarmPhotoUrl());
        }
        if (req.getBrandIntro() != null) {
            product.setBrandIntro(req.getBrandIntro());
        }
        if (req.getBrandLogoUrl() != null) {
            product.setBrandLogoUrl(req.getBrandLogoUrl());
        }

        productRepository.save(product);
        return Result.success(product);
    }
}
