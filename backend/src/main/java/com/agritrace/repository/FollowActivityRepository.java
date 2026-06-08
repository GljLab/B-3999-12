package com.agritrace.repository;

import com.agritrace.entity.FollowActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowActivityRepository extends JpaRepository<FollowActivity, Long> {

    @Query("SELECT a FROM FollowActivity a WHERE a.userId IN :userIds ORDER BY a.createdAt DESC")
    Page<FollowActivity> findByUserIdInOrderByCreatedAtDesc(@Param("userIds") List<Long> userIds, Pageable pageable);

    List<FollowActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
