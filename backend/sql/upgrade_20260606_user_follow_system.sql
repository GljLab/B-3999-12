-- 用户收藏体系与个人主页系统升级脚本
-- 执行时间: 2026-06-06

USE `agri_trace`;

-- 1. 为 sys_user 表添加个性签名字段和头像字段
ALTER TABLE `sys_user` 
ADD COLUMN `signature` VARCHAR(100) DEFAULT NULL COMMENT '个性签名' AFTER `phone`,
ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL' AFTER `signature`,
ADD COLUMN `last_active_at` DATETIME DEFAULT NULL COMMENT '最近活跃时间' AFTER `updated_at`;

-- 2. 创建用户收藏关系表
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` BIGINT NOT NULL COMMENT '收藏者用户ID',
  `followed_id` BIGINT NOT NULL COMMENT '被收藏者用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_followed`(`follower_id`, `followed_id`) USING BTREE,
  INDEX `idx_follower_id`(`follower_id`) USING BTREE,
  INDEX `idx_followed_id`(`followed_id`) USING BTREE,
  CONSTRAINT `fk_follower_user` FOREIGN KEY (`follower_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_followed_user` FOREIGN KEY (`followed_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏关系表';

-- 3. 创建收藏动态表
DROP TABLE IF EXISTS `follow_activity`;
CREATE TABLE `follow_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '产生动态的用户ID',
  `activity_type` VARCHAR(50) NOT NULL COMMENT '动态类型: NEW_POST, POST_HOT, NEW_PRODUCT, PRODUCT_HOT, NEW_SUPPORTER, NEW_FOLLOW',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `extra_data` JSON DEFAULT NULL COMMENT '额外数据(JSON格式)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏用户动态表';

-- 4. 为现有用户设置默认最近活跃时间
UPDATE `sys_user` SET `last_active_at` = `updated_at` WHERE `last_active_at` IS NULL;
