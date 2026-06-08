package com.agritrace.repository;

import com.agritrace.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateNo(String certificateNo);
    Page<Certificate> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Certificate> findByProductIdAndStatus(Long productId, Integer status);
    List<Certificate> findByTraceCodeIdAndStatus(Long traceCodeId, Integer status);
    long countByProductIdAndStatus(Long productId, Integer status);
    long countByUserIdAndStatus(Long userId, Integer status);

    @Query("SELECT c.productId, COUNT(c) as cnt FROM Certificate c WHERE c.status = 1 GROUP BY c.productId ORDER BY cnt DESC")
    List<Object[]> countByProductTop(Pageable pageable);

    @Query("SELECT p.farmerId, COUNT(c) as cnt FROM Certificate c JOIN Product p ON c.productId = p.id WHERE c.status = 1 GROUP BY p.farmerId ORDER BY cnt DESC")
    List<Object[]> countByFarmerTop(Pageable pageable);

    @Query("SELECT DATE(c.createdAt) as d, COUNT(c) as cnt FROM Certificate c WHERE c.createdAt >= :start GROUP BY DATE(c.createdAt) ORDER BY d")
    List<Object[]> countDaily(@Param("start") LocalDateTime start);

    @Query("SELECT c.templateType, COUNT(c) as cnt FROM Certificate c WHERE c.status = 1 GROUP BY c.templateType")
    List<Object[]> countByTemplateType();

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(Integer status);
}
