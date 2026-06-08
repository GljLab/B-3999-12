-- Upgrade: Certificate system tables and product customization fields
-- Date: 2026-06-08

USE `agri_trace`;

ALTER TABLE `product` ADD COLUMN `farm_photo_url` VARCHAR(500) DEFAULT NULL COMMENT '农场照片URL' AFTER `image_url`;
ALTER TABLE `product` ADD COLUMN `brand_intro` TEXT DEFAULT NULL COMMENT '品牌介绍语' AFTER `farm_photo_url`;
ALTER TABLE `product` ADD COLUMN `brand_logo_url` VARCHAR(500) DEFAULT NULL COMMENT '品牌标识URL' AFTER `brand_intro`;

DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `certificate_no` VARCHAR(32) NOT NULL COMMENT '唯一证书编号',
  `user_id` BIGINT NOT NULL COMMENT '生成证书的用户ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `trace_code_id` BIGINT NOT NULL COMMENT '溯源码ID',
  `batch_id` BIGINT DEFAULT NULL COMMENT '批次ID',
  `template_type` VARCHAR(20) NOT NULL DEFAULT 'CLASSIC' COMMENT '模板类型: CLASSIC, MINIMAL, CHINESE, TECH',
  `product_name` VARCHAR(100) NOT NULL COMMENT '快照: 产品名称',
  `product_category` VARCHAR(50) DEFAULT NULL COMMENT '快照: 产品类别',
  `product_origin` VARCHAR(255) DEFAULT NULL COMMENT '快照: 产地',
  `product_description` TEXT DEFAULT NULL COMMENT '快照: 产品描述',
  `product_image_url` VARCHAR(500) DEFAULT NULL COMMENT '快照: 产品图片URL',
  `harvest_date` DATE DEFAULT NULL COMMENT '快照: 采摘日期',
  `farmer_name` VARCHAR(50) DEFAULT NULL COMMENT '快照: 农户名称',
  `farm_photo_url` VARCHAR(500) DEFAULT NULL COMMENT '快照: 农场照片',
  `brand_intro` TEXT DEFAULT NULL COMMENT '快照: 品牌介绍',
  `brand_logo_url` VARCHAR(500) DEFAULT NULL COMMENT '快照: 品牌标识',
  `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '快照: 批次编号',
  `production_date` DATE DEFAULT NULL COMMENT '快照: 生产日期',
  `quality_grade` VARCHAR(20) DEFAULT NULL COMMENT '快照: 质量等级',
  `logistics_summary` TEXT DEFAULT NULL COMMENT '快照: 物流轨迹JSON',
  `trace_code` VARCHAR(64) NOT NULL COMMENT '快照: 溯源码',
  `digital_signature` VARCHAR(128) DEFAULT NULL COMMENT '数字签名',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享次数',
  `verify_count` INT NOT NULL DEFAULT 0 COMMENT '验证次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-有效, 0-已作废',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_certificate_no`(`certificate_no`) USING BTREE,
  INDEX `idx_cert_user`(`user_id`) USING BTREE,
  INDEX `idx_cert_product`(`product_id`) USING BTREE,
  INDEX `idx_cert_trace_code`(`trace_code_id`) USING BTREE,
  INDEX `idx_cert_status`(`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源证书表';

DROP TABLE IF EXISTS `certificate_share_log`;
CREATE TABLE `certificate_share_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `certificate_id` BIGINT NOT NULL COMMENT '证书ID',
  `share_type` VARCHAR(20) NOT NULL COMMENT '分享类型: COMMUNITY, QRCODE, POSTER',
  `share_user_id` BIGINT NOT NULL COMMENT '分享用户ID',
  `post_id` BIGINT DEFAULT NULL COMMENT '关联社区帖子ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_share_cert`(`certificate_id`) USING BTREE,
  INDEX `idx_share_user`(`share_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书分享记录表';

DROP TABLE IF EXISTS `certificate_view_log`;
CREATE TABLE `certificate_view_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `certificate_id` BIGINT NOT NULL COMMENT '证书ID',
  `viewer_ip` VARCHAR(50) DEFAULT NULL COMMENT '查看者IP',
  `source` VARCHAR(20) NOT NULL DEFAULT 'DIRECT' COMMENT '来源: DIRECT, SHARE, QRCODE, VERIFY',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查看时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_view_cert`(`certificate_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书查看记录表';
