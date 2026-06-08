package com.agritrace.repository;
import com.agritrace.entity.TracingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface TracingCodeRepository extends JpaRepository<TracingCode, Long> {
    Optional<TracingCode> findByTraceCode(String traceCode);
    List<TracingCode> findByProductId(Long productId);
    List<TracingCode> findByBatchId(Long batchId);
    boolean existsByBatchId(Long batchId);
    Optional<TracingCode> findTopByBatchIdOrderByGeneratedAtDesc(Long batchId);

    @Query("SELECT COUNT(t) FROM TracingCode t WHERE t.productId IN :productIds")
    long countByProductIdIn(@Param("productIds") List<Long> productIds);
}
