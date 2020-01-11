DROP TABLE IF EXISTS `sequence`;

CREATE TABLE `sequence` (
      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
      `application` varchar(64) NOT NULL DEFAULT 'default-application' COMMENT '应用名',
      `sequence_name` varchar(255) NOT NULL COMMENT '序列号名',
      `step` bigint(20) unsigned NOT NULL DEFAULT '300' COMMENT '每次递增步长',
      `min` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT '分区最小序号',
      `max` bigint(20) unsigned NOT NULL DEFAULT '4294967295' COMMENT '分区最大序号',
      `current_segment` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT '分区当前可用序号',
      `segment_unit` int(11) NOT NULL DEFAULT '300' COMMENT '每个分区包含序号数',
      `version` bigint(20) NOT NULL DEFAULT '1' COMMENT '自增版本号',
      PRIMARY KEY (`id`),
      UNIQUE KEY `idx_uniq_name` (`application`,`sequence_name`) USING BTREE
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Sequence表';

DROP TABLE IF EXISTS `segment`;

CREATE TABLE `segment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `application` varchar(64) NOT NULL DEFAULT 'default-application' COMMENT '应用名',
    `sequence_name` varchar(255) NOT NULL DEFAULT 'default_sequence' COMMENT '序列名',
    `current_segment` bigint(20) NOT NULL DEFAULT '1' COMMENT '当前段',
    `min` bigint(20) NOT NULL DEFAULT '1' COMMENT '最小值',
    `max` bigint(20) NOT NULL DEFAULT '2147483647' COMMENT '最大值',
    `current` bigint(20) NOT NULL DEFAULT '0' COMMENT '当前值',
    `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '更新版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_segment` (`application`,`sequence_name`) USING BTREE
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;