package com.agritrace.repository;

import com.agritrace.entity.CertificateViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CertificateViewLogRepository extends JpaRepository<CertificateViewLog, Long> {
    long countByCertificateId(Long certificateId);

    @Query("SELECT COUNT(DISTINCT v.certificateId) FROM CertificateViewLog v WHERE v.source = 'SHARE' AND v.createdAt >= :start")
    long countShareViewsSince(@Param("start") LocalDateTime start);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
