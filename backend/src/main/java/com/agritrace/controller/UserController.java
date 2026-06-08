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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserFollowRepository userFollowRepository;
    @Autowired
    private CommunityPostRepository communityPostRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private PostBookmarkRepository postBookmarkRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TracingCodeRepository tracingCodeRepository;
    @Autowired
    private LogisticsRepository logisticsRepository;
    @Autowired
    private FollowActivityRepository followActivityRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private UserTopicFollowRepository userTopicFollowRepository;
    @Autowired
    private PostTopicRepository postTopicRepository;

    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            Object uid = request.getAttribute("userId");
            if (uid != null) {
                return ((Number) uid).longValue();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @GetMapping("/{id}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long id, HttpServletRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Long currentUserId = getCurrentUserId(request);
        boolean isOwner = currentUserId != null && currentUserId.equals(id);

        long followerCount = userFollowRepository.countByFollowedId(id);
        long followingCount = userFollowRepository.countByFollowerId(id);
        long mutualCount = userFollowRepository.countMutualFollows(id);

        UserVO userVO = UserVO.from(user, followerCount, followingCount, mutualCount);
        if (currentUserId != null && !isOwner) {
            userVO.setIsFollowed(userFollowRepository.existsByFollowerIdAndFollowedId(currentUserId, id));
            userVO.setIsMutual(userFollowRepository.existsByFollowerIdAndFollowedId(id, currentUserId));
        }

        long daysJoined = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now());

        Map<String, Object> stats = new HashMap<>();
        stats.put("postCount", communityPostRepository.findByUserIdOrderByCreatedAtDesc(id).size());
        stats.put("totalLikes", postLikeRepository.countByUserId(id));
        stats.put("totalBookmarks", postBookmarkRepository.countByUserId(id));
        stats.put("commentCount", postCommentRepository.countByUserId(id));
        stats.put("lastActiveAt", user.getLastActiveAt());

        Map<String, Object> roleStats = getRoleStats(user);

        UserProfileVO profile = UserProfileVO.from(userVO, stats, roleStats, daysJoined, isOwner);
        return Result.success(profile);
    }

    private Map<String, Object> getRoleStats(User user) {
        Map<String, Object> roleStats = new HashMap<>();
        String role = user.getRole();

        if ("FARMER".equals(role)) {
            List<Product> products = productRepository.findByFarmerId(user.getId());
            roleStats.put("productCount", products.size());
            roleStats.put("tracingCodeCount", tracingCodeRepository.countByProductIdIn(
                products.stream().map(Product::getId).toList()
            ));
            Set<String> categories = products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            roleStats.put("mainCategories", new ArrayList<>(categories));
        } else if ("LOGS_ADMIN".equals(role)) {
            roleStats.put("logisticsCount", logisticsRepository.countByLogisticsAdminId(user.getId()));
            Set<String> locations = logisticsRepository.findByLogisticsAdminId(user.getId())
                .stream()
                .map(Logistics::getLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            roleStats.put("serviceAreas", new ArrayList<>(locations));
        } else if ("SYS_ADMIN".equals(role)) {
            roleStats.put("managedUserCount", userRepository.count());
            roleStats.put("managedTopicCount", topicRepository.count());
            roleStats.put("communityContentCount", communityPostRepository.count());
        }

        return roleStats;
    }

    @PutMapping("/profile")
    @Transactional
    public Result<UserVO> updateProfile(HttpServletRequest request, @RequestBody UserUpdateRequest req) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (req.getSignature() != null) {
            if (req.getSignature().length() > 100) {
                return Result.error(400, "个性签名最多100字");
            }
            user.setSignature(req.getSignature().trim());
        }
        if (req.getRealName() != null) {
            user.setRealName(req.getRealName().trim());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone().trim());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar().trim());
        }
        userRepository.save(user);

        long followerCount = userFollowRepository.countByFollowedId(userId);
        long followingCount = userFollowRepository.countByFollowerId(userId);
        long mutualCount = userFollowRepository.countMutualFollows(userId);

        return Result.success(UserVO.from(user, followerCount, followingCount, mutualCount));
    }

    @PostMapping("/{id}/follow")
    @Transactional
    public Result<Map<String, Object>> toggleFollow(HttpServletRequest request, @PathVariable Long id) {
        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();

        if (currentUserId.equals(id)) {
            return Result.error(400, "不能收藏自己");
        }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) {
            return Result.error(404, "用户不存在");
        }

        Optional<UserFollow> existing = userFollowRepository.findByFollowerIdAndFollowedId(currentUserId, id);
        boolean followed;

        if (existing.isPresent()) {
            userFollowRepository.delete(existing.get());
            followed = false;
        } else {
            UserFollow follow = new UserFollow();
            follow.setFollowerId(currentUserId);
            follow.setFollowedId(id);
            userFollowRepository.save(follow);
            followed = true;

            FollowActivity activity = new FollowActivity();
            activity.setUserId(currentUserId);
            activity.setActivityType("NEW_FOLLOW");
            activity.setTargetId(id);
            followActivityRepository.save(activity);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        result.put("followerCount", userFollowRepository.countByFollowedId(id));
        result.put("followingCount", userFollowRepository.countByFollowerId(currentUserId));
        result.put("isMutual", userFollowRepository.existsByFollowerIdAndFollowedId(id, currentUserId));

        return Result.success(result);
    }

    @GetMapping("/{id}/posts")
    public Result<Map<String, Object>> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String filter) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        List<CommunityPost> allPosts = communityPostRepository.findByUserIdOrderByCreatedAtDesc(id);

        if ("hasImage".equals(filter)) {
            allPosts = allPosts.stream()
                .filter(p -> p.getImages() != null && !p.getImages().trim().isEmpty())
                .toList();
        } else if ("highLike".equals(filter)) {
            allPosts = allPosts.stream()
                .filter(p -> p.getLikeCount() >= 10)
                .toList();
        }

        if ("likes".equals(sort)) {
            allPosts = allPosts.stream()
                .sorted(Comparator.comparingInt(CommunityPost::getLikeCount).reversed())
                .toList();
        } else if ("comments".equals(sort)) {
            allPosts = allPosts.stream()
                .sorted(Comparator.comparingInt(CommunityPost::getCommentCount).reversed())
                .toList();
        }

        int from = Math.min(page * size, allPosts.size());
        int to = Math.min((page + 1) * size, allPosts.size());
        List<CommunityPost> pagedPosts = from < to ? allPosts.subList(from, to) : List.of();

        List<CommunityPostVO> voList = pagedPosts.stream()
            .map(post -> {
                CommunityPostVO vo = CommunityPostVO.from(post, user);
                List<Long> topicIds = postTopicRepository.findTopicIdsByPostId(post.getId());
                List<Topic> topics = topicRepository.findByIdIn(topicIds);
                vo.setTopics(topics.stream().map(TopicVO::from).toList());
                return vo;
            })
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", voList);
        result.put("totalElements", allPosts.size());
        result.put("totalPages", (int) Math.ceil((double) allPosts.size() / size));
        result.put("currentPage", page);
        result.put("last", (page + 1) * size >= allPosts.size());

        return Result.success(result);
    }

    @GetMapping("/{id}/following")
    public Result<Map<String, Object>> getFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Long currentUserId = getCurrentUserId(request);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFollow> followPage = userFollowRepository.findByFollowerIdOrderByCreatedAtDesc(id, pageable);

        Set<Long> currentFollowing = new HashSet<>();
        if (currentUserId != null) {
            currentFollowing.addAll(userFollowRepository.getFollowedUserIds(currentUserId));
        }

        Set<Long> mutualCheckIds = followPage.getContent().stream()
            .map(UserFollow::getFollowedId)
            .collect(Collectors.toSet());
        Set<Long> mutualFollowers = new HashSet<>();
        if (!mutualCheckIds.isEmpty()) {
            mutualFollowers.addAll(userFollowRepository.getFollowerUserIds(id));
        }

        List<Long> followedUserIds = followPage.getContent().stream()
            .map(UserFollow::getFollowedId).toList();
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(followedUserIds).forEach(u -> userMap.put(u.getId(), u));

        Map<Long, UserFollow> followMap = followPage.getContent().stream()
            .collect(Collectors.toMap(UserFollow::getFollowedId, f -> f));

        List<FollowUserVO> voList = followedUserIds.stream()
            .map(uid -> {
                User u = userMap.get(uid);
                if (u == null) return null;
                long fc = userFollowRepository.countByFollowedId(uid);
                long foc = userFollowRepository.countByFollowerId(uid);
                boolean isMutual = mutualFollowers.contains(uid);
                FollowUserVO vo = FollowUserVO.from(u, followMap.get(uid), fc, foc, isMutual);
                return vo;
            })
            .filter(Objects::nonNull)
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", voList);
        result.put("totalElements", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("currentPage", followPage.getNumber());
        result.put("last", followPage.isLast());

        return Result.success(result);
    }

    @GetMapping("/{id}/followers")
    public Result<Map<String, Object>> getFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Long currentUserId = getCurrentUserId(request);
        boolean isOwner = currentUserId != null && currentUserId.equals(id);
        if (!isOwner) {
            return Result.error(403, "仅本人可查看支持者列表");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFollow> followPage = userFollowRepository.findByFollowedIdOrderByCreatedAtDesc(id, pageable);

        Set<Long> currentFollowing = new HashSet<>();
        if (currentUserId != null) {
            currentFollowing.addAll(userFollowRepository.getFollowedUserIds(currentUserId));
        }

        List<Long> followerUserIds = followPage.getContent().stream()
            .map(UserFollow::getFollowerId).toList();
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(followerUserIds).forEach(u -> userMap.put(u.getId(), u));

        Map<Long, UserFollow> followMap = followPage.getContent().stream()
            .collect(Collectors.toMap(UserFollow::getFollowerId, f -> f));

        List<FollowUserVO> voList = followerUserIds.stream()
            .map(uid -> {
                User u = userMap.get(uid);
                if (u == null) return null;
                long fc = userFollowRepository.countByFollowedId(uid);
                long foc = userFollowRepository.countByFollowerId(uid);
                boolean isMutual = currentFollowing.contains(uid);
                FollowUserVO vo = FollowUserVO.from(u, followMap.get(uid), fc, foc, isMutual);
                return vo;
            })
            .filter(Objects::nonNull)
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", voList);
        result.put("totalElements", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("currentPage", followPage.getNumber());
        result.put("last", followPage.isLast());

        return Result.success(result);
    }

    @GetMapping("/{id}/products")
    public Result<List<Product>> getUserProducts(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (!"FARMER".equals(user.getRole())) {
            return Result.error(400, "该用户不是农户");
        }
        List<Product> products = productRepository.findByFarmerId(id);
        return Result.success(products);
    }

    @GetMapping("/{id}/logistics")
    public Result<Map<String, Object>> getUserLogistics(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (!"LOGS_ADMIN".equals(user.getRole())) {
            return Result.error(400, "该用户不是物流管理员");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt"));
        Page<Logistics> logisticsPage = logisticsRepository.findByLogisticsAdminIdOrderByRecordedAtDesc(id, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("content", logisticsPage.getContent());
        result.put("totalElements", logisticsPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/follow-feed")
    public Result<Map<String, Object>> getFollowFeed(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();
        List<Long> followedIds = userFollowRepository.getFollowedUserIds(currentUserId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowActivity> activityPage;

        if (followedIds.isEmpty()) {
            activityPage = Page.empty();
        } else {
            activityPage = followActivityRepository.findByUserIdInOrderByCreatedAtDesc(followedIds, pageable);
        }

        Set<Long> userIds = activityPage.getContent().stream()
            .map(FollowActivity::getUserId)
            .collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));

        List<FollowActivityVO> voList = activityPage.getContent().stream()
            .map(a -> FollowActivityVO.from(a, userMap.get(a.getUserId())))
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", voList);
        result.put("totalElements", activityPage.getTotalElements());
        result.put("totalPages", activityPage.getTotalPages());
        result.put("currentPage", activityPage.getNumber());
        result.put("last", activityPage.isLast());
        result.put("isEmpty", followedIds.isEmpty());

        return Result.success(result);
    }

    @GetMapping("/recommend")
    public Result<List<UserVO>> getRecommendUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {

        Long currentUserId = getCurrentUserId(request);
        Set<Long> excludeIds = new HashSet<>();
        if (currentUserId != null) {
            excludeIds.add(currentUserId);
            excludeIds.addAll(userFollowRepository.getFollowedUserIds(currentUserId));
        }

        List<User> allUsers = userRepository.findAll();
        List<User> candidates = allUsers.stream()
            .filter(u -> !excludeIds.contains(u.getId()))
            .filter(u -> u.getEnabled() != null && u.getEnabled() == 1)
            .toList();

        Map<Long, Double> scores = new HashMap<>();
        Set<Long> interactedUsers = new HashSet<>();

        if (currentUserId != null) {
            List<Long> likedPostIds = postLikeRepository.findPostIdsByUserId(currentUserId);
            for (Long postId : likedPostIds) {
                communityPostRepository.findById(postId).ifPresent(p -> interactedUsers.add(p.getUserId()));
            }
            List<Long> commentedPostIds = postCommentRepository.findPostIdsByUserId(currentUserId);
            for (Long postId : commentedPostIds) {
                communityPostRepository.findById(postId).ifPresent(p -> interactedUsers.add(p.getUserId()));
            }
        }

        for (User user : candidates) {
            double score = 0;
            if (interactedUsers.contains(user.getId())) {
                score += 50;
            }

            long followerCount = userFollowRepository.countByFollowedId(user.getId());
            score += Math.min(followerCount * 0.1, 30);

            int postCount = communityPostRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
            score += Math.min(postCount * 2, 20);

            score += Math.random() * 10;
            scores.put(user.getId(), score);
        }

        List<User> recommended = candidates.stream()
            .sorted(Comparator.comparingDouble((User u) -> scores.getOrDefault(u.getId(), 0.0)).reversed())
            .limit(limit)
            .toList();

        List<UserVO> voList = recommended.stream()
            .map(u -> {
                long fc = userFollowRepository.countByFollowedId(u.getId());
                long foc = userFollowRepository.countByFollowerId(u.getId());
                UserVO vo = UserVO.from(u, fc, foc, 0L);
                vo.setIsFollowed(false);
                return vo;
            })
            .toList();

        return Result.success(voList);
    }

    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalFollows = userFollowRepository.count();
        long totalUsers = userRepository.count();
        double avgFollows = totalUsers > 0 ? (double) totalFollows / totalUsers : 0;

        stats.put("totalFollows", totalFollows);
        stats.put("avgFollowsPerUser", Math.round(avgFollows * 100.0) / 100.0);
        stats.put("totalUsers", totalUsers);

        List<Object[]> trendData = userFollowRepository.getFollowGrowthTrend(30);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] row : trendData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            trend.add(item);
        }
        stats.put("growthTrend", trend);

        return Result.success(stats);
    }

    @GetMapping("/admin/rankings/followers")
    public Result<Map<String, Object>> getFollowerRankings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String role) {

        List<User> users = userRepository.findAll();
        if (role != null && !role.trim().isEmpty()) {
            users = users.stream().filter(u -> role.equals(u.getRole())).toList();
        }

        List<Map<String, Object>> rankings = users.stream()
            .map(u -> {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", u.getId());
                item.put("username", u.getUsername());
                item.put("realName", u.getRealName() != null ? u.getRealName() : u.getUsername());
                item.put("role", u.getRole());
                item.put("followerCount", userFollowRepository.countByFollowedId(u.getId()));
                item.put("followingCount", userFollowRepository.countByFollowerId(u.getId()));
                return item;
            })
            .sorted(Comparator.comparingLong((Map<String, Object> m) -> (Long) m.get("followerCount")).reversed())
            .toList();

        int from = Math.min(page * size, rankings.size());
        int to = Math.min((page + 1) * size, rankings.size());

        Map<String, Object> result = new HashMap<>();
        result.put("content", from < to ? rankings.subList(from, to) : List.of());
        result.put("totalElements", rankings.size());
        result.put("totalPages", (int) Math.ceil((double) rankings.size() / size));

        return Result.success(result);
    }
}
