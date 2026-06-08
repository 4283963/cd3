-- =============================================
-- 沉浸式密室剧本杀平台 数据库初始化脚本
-- Database: MySQL 8.0+
-- =============================================

CREATE DATABASE IF NOT EXISTS script_kill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE script_kill;

-- =============================================
-- 1. 管理员/导演表
-- =============================================
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员/导演表';

-- =============================================
-- 2. 剧本表
-- =============================================
DROP TABLE IF EXISTS `script`;
CREATE TABLE `script` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '剧本ID',
  `title` varchar(100) NOT NULL COMMENT '剧本名称',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `description` text COMMENT '剧本简介',
  `difficulty` tinyint NOT NULL DEFAULT '2' COMMENT '难度：1-简单 2-中等 3-困难',
  `duration` int DEFAULT NULL COMMENT '预计时长（分钟）',
  `player_count_min` int NOT NULL DEFAULT '2' COMMENT '最少玩家数',
  `player_count_max` int NOT NULL DEFAULT '8' COMMENT '最多玩家数',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-上架 0-下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧本表';

-- =============================================
-- 3. 角色表
-- =============================================
DROP TABLE IF EXISTS `script_role`;
CREATE TABLE `script_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `script_id` bigint NOT NULL COMMENT '剧本ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `gender` tinyint DEFAULT NULL COMMENT '性别：1-男 2-女 0-不限',
  `avatar` varchar(255) DEFAULT NULL COMMENT '角色头像',
  `description` varchar(500) DEFAULT NULL COMMENT '角色简介',
  `background_story` text COMMENT '角色背景故事（剧本）',
  `secret` text COMMENT '角色秘密',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_script_id` (`script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 4. 线索表（线索树结构）
-- =============================================
DROP TABLE IF EXISTS `clue`;
CREATE TABLE `clue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `script_id` bigint NOT NULL COMMENT '剧本ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父线索ID（0为根节点）',
  `name` varchar(100) NOT NULL COMMENT '线索名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1-文字 2-图片 3-音频 4-视频',
  `content` text COMMENT '线索内容（文字）',
  `resource_url` varchar(500) DEFAULT NULL COMMENT '资源URL（图片/音频/视频）',
  `unlock_password` varchar(100) DEFAULT NULL COMMENT '解锁密码（明文，用于匹配）',
  `unlock_hint` varchar(255) DEFAULT NULL COMMENT '解锁提示',
  `target_role_ids` varchar(255) DEFAULT NULL COMMENT '指定可见角色ID列表，空表示全部可见',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开线索：1-是 0-否',
  `level` int NOT NULL DEFAULT '1' COMMENT '层级',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_puzzle` tinyint NOT NULL DEFAULT '0' COMMENT '是否拼图解锁：1-是 0-否',
  `puzzle_rows` int NOT NULL DEFAULT '3' COMMENT '拼图行数',
  `puzzle_cols` int NOT NULL DEFAULT '3' COMMENT '拼图列数',
  `puzzle_time_limit` int NOT NULL DEFAULT '180' COMMENT '拼图时间限制（秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_script_id` (`script_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线索表';

-- =============================================
-- 5. 游戏场次表
-- =============================================
DROP TABLE IF EXISTS `game_session`;
CREATE TABLE `game_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `script_id` bigint NOT NULL COMMENT '剧本ID',
  `session_code` varchar(20) NOT NULL COMMENT '房间码（6位）',
  `director_id` bigint NOT NULL COMMENT '导演ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待开始 1-进行中 2-已结束 3-已暂停',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `current_stage` varchar(50) DEFAULT 'intro' COMMENT '当前阶段',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_code` (`session_code`),
  KEY `idx_script_id` (`script_id`),
  KEY `idx_director_id` (`director_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏场次表';

-- =============================================
-- 6. 玩家表
-- =============================================
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `role_id` bigint DEFAULT NULL COMMENT '分配的角色ID',
  `nickname` varchar(50) NOT NULL COMMENT '玩家昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '玩家头像',
  `is_online` tinyint NOT NULL DEFAULT '0' COMMENT '是否在线：1-是 0-否',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家表';

-- =============================================
-- 7. 玩家线索表（玩家已获取的线索）
-- =============================================
DROP TABLE IF EXISTS `player_clue`;
CREATE TABLE `player_clue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `clue_id` bigint NOT NULL COMMENT '线索ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `is_unlocked` tinyint NOT NULL DEFAULT '0' COMMENT '是否已解锁：1-是 0-否',
  `unlock_time` datetime DEFAULT NULL COMMENT '解锁时间',
  `distributed_by` bigint DEFAULT NULL COMMENT '分发人ID（导演）',
  `distribute_time` datetime DEFAULT NULL COMMENT '分发时间',
  `puzzle_status` tinyint NOT NULL DEFAULT '0' COMMENT '拼图状态：0-未开始 1-进行中 2-已完成 3-已销毁',
  `puzzle_start_time` datetime DEFAULT NULL COMMENT '拼图开始时间',
  `puzzle_current` text COMMENT '当前拼图排列（JSON数组）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_clue` (`player_id`, `clue_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_clue_id` (`clue_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家线索表';

-- =============================================
-- 8. 玩家进度表
-- =============================================
DROP TABLE IF EXISTS `player_progress`;
CREATE TABLE `player_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `current_node` varchar(100) DEFAULT 'start' COMMENT '当前剧情节点',
  `progress_data` json DEFAULT NULL COMMENT '进度数据（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_session` (`player_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家进度表';

-- =============================================
-- 9. 导演消息表（导演向玩家发送的消息）
-- =============================================
DROP TABLE IF EXISTS `director_message`;
CREATE TABLE `director_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `sender_id` bigint NOT NULL COMMENT '发送人ID（导演）',
  `receiver_type` tinyint NOT NULL COMMENT '接收类型：1-全部玩家 2-指定玩家',
  `receiver_ids` varchar(500) DEFAULT NULL COMMENT '接收玩家ID列表（逗号分隔）',
  `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1-系统消息 2-剧情提示 3-线索通知',
  `title` varchar(100) DEFAULT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读（至少一人已读）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导演消息表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员（密码: 123456，MD5加密: e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `sys_admin` (`username`, `password`, `nickname`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级导演', 1),
('director', 'e10adc3949ba59abbe56e057f20f883e', 'DM小助手', 1);

-- 插入示例剧本
INSERT INTO `script` (`title`, `cover_image`, `description`, `difficulty`, `duration`, `player_count_min`, `player_count_max`, `author`, `status`) VALUES
('古宅惊魂', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ancient%20chinese%20mansion%20horror%20mystery%20at%20night&image_size=landscape_16_9', '民国时期，一座神秘的古宅中发生了离奇命案。你们作为受邀而来的宾客，被困在宅中，必须找出真凶才能离开...', 2, 180, 4, 8, '剧本工作室', 1),
('星际迷航', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=space%20exploration%20sci-fi%20spaceship%20galaxy&image_size=landscape_16_9', '2157年，星际探索号飞船在执行任务时遭遇意外。飞船上的船员们必须在有限的时间内修复飞船并找出破坏者...', 3, 240, 5, 10, '科幻创作组', 1);

-- 插入示例角色（剧本1：古宅惊魂）
INSERT INTO `script_role` (`script_id`, `name`, `gender`, `avatar`, `description`, `background_story`, `sort_order`) VALUES
(1, '大小姐 林雪', 2, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20noble%20lady%20in%20qipao%20elegant%20portrait&image_size=square', '林府的大小姐，温婉端庄', '你是林府的大小姐林雪。三年前你的母亲病逝，父亲续弦娶了继母。你一直怀疑母亲的死另有隐情，但苦于没有证据...', 1),
(1, '管家 王福', 1, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=old%20chinese%20butler%20serious%20man%20portrait&image_size=square', '林府的老管家，忠心耿耿', '你是林府的管家王福，在林府服务了三十年。你见证了林家的兴衰，也知道许多不为人知的秘密...', 2),
(1, '医生 陈铭', 1, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=young%20chinese%20doctor%20gentleman%20portrait&image_size=square', '年轻有为的医生，受邀前来', '你是城中的名医陈铭。林家老爷病重，你被请来诊治。但你还有另一个秘密身份...', 3),
(1, '女仆 小芳', 2, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=young%20chinese%20maid%20innocent%20girl%20portrait&image_size=square', '刚来不久的女仆，单纯可爱', '你是新来的女仆小芳。你无意中发现了一些关于这座宅子的恐怖秘密...', 4);

-- 插入示例线索（剧本1：古宅惊魂）
INSERT INTO `clue` (`script_id`, `parent_id`, `name`, `type`, `content`, `resource_url`, `unlock_password`, `unlock_hint`, `is_public`, `level`, `sort_order`, `is_puzzle`, `puzzle_rows`, `puzzle_cols`, `puzzle_time_limit`) VALUES
(1, 0, '初始线索：一封遗书', 1, '死者书房中发现一封遗书，上面写着："我知道你们的秘密，今晚一切都将了结。"', NULL, NULL, NULL, 1, 1, 1, 0, 3, 3, 180),
(1, 0, '线索：带血的匕首', 2, '一把沾有血迹的匕首，被丢弃在花园的花丛中', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=blood%20dagger%20murder%20weapon%20dark%20background&image_size=square', NULL, NULL, 1, 1, 2, 0, 3, 3, 180),
(1, 1, '隐藏线索：日记残页', 1, '日记上写着："三月十五，她终于说出了真相。原来那孩子..."后面的内容被撕掉了。', NULL, '19350315', '想想故事发生的年代，月份和日期的格式...', 0, 2, 1, 0, 3, 3, 180),
(1, 2, '隐藏线索：匕首上的指纹', 2, '经过仔细观察，匕首上有一枚清晰的指纹，指纹的小指侧有一道疤痕', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fingerprint%20on%20dagger%20close%20up%20forensic&image_size=square', '管家', '想想谁会有这样的特征...', 0, 2, 2, 0, 3, 3, 180),
(1, 0, '机密线索：神秘照片', 2, '一张泛黄的老照片，照片上是年轻时的林老爷和一位陌生女子...', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=vintage%20old%20photo%20chinese%20couple%20mysterious&image_size=square', NULL, '完成拼图即可解锁', 0, 2, 3, 1, 3, 3, 180),
(1, 3, '终极线索：真相大白', 1, '原来，大小姐林雪才是真正的凶手。她发现母亲是被继母害死的，于是策划了这场复仇...', NULL, '真相', '一切水落石出之时...', 0, 3, 1, 0, 3, 3, 180);

SELECT '数据库初始化完成！' AS 'INFO';
