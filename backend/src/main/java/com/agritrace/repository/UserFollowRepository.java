package com.agritrace.repository;

import com.agritrace.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    long countByFollowerId(Long followerId);

    long countByFollowedId(Long followedId);

    List<Long> findFollowedIdsByFollowerId(Long followerId);

    List<Long> findFollowerIdsByFollowedId(Long followedId);

    Page<UserFollow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    Page<UserFollow> findByFollowedIdOrderByCreatedAtDesc(Long followedId, Pageable pageable);

    @Query("SELECT f.followedId FROM UserFollow f WHERE f.followerId = :followerId")
    List<Long> getFollowedUserIds(@Param("followerId") Long followerId);

    @Query("SELECT f.followerId FROM UserFollow f WHERE f.followedId = :followedId")
    List<Long> getFollowerUserIds(@Param("followedId") Long followedId);

    @Query("SELECT COUNT(f) FROM UserFollow f WHERE f.followerId = :userId AND f.followedId IN " +
           "(SELECT f2.followerId FROM UserFollow f2 WHERE f2.followedId = :userId)")
    long countMutualFollows(@Param("userId") Long userId);

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as date, " +
                   "SUM(CASE WHEN id IS NOT NULL THEN 1 ELSE 0 END) as count " +
                   "FROM user_follow " +
                   "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
                   "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') " +
                   "ORDER BY date", nativeQuery = true)
    List<Object[]> getFollowGrowthTrend(@Param("days") int days);
}
