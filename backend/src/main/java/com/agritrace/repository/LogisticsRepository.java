package com.agritrace.repository;
import com.agritrace.entity.Logistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LogisticsRepository extends JpaRepository<Logistics, Long> {
    List<Logistics> findByTraceCodeId(Long traceCodeId);
    boolean existsByLogisticsAdminId(Long logisticsAdminId);
    long countByLogisticsAdminId(Long logisticsAdminId);
    List<Logistics> findByLogisticsAdminId(Long logisticsAdminId);
    Page<Logistics> findByLogisticsAdminIdOrderByRecordedAtDesc(Long logisticsAdminId, Pageable pageable);
}
