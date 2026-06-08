package com.agritrace.repository;

import com.agritrace.entity.CertificateShareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateShareLogRepository extends JpaRepository<CertificateShareLog, Long> {
    List<CertificateShareLog> findByCertificateIdOrderByCreatedAtDesc(Long certificateId);
    long countByCertificateId(Long certificateId);
    long countByShareUserId(Long userId);
    long countByShareType(String shareType);
}
