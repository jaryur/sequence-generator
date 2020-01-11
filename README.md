# Introduction
 s simple sequence generator based on mysql, but do not guarantee exactly overall seriality
 
# Feature

1. Advanced performance by caching sequence locally
2. Asynchronous loading when sequence is almost used up
3. Lazy mode supported
4. Single sequence & Sequence range


# Quick Start 

1. maven dependency

    ```
    <dependency>
        <groupId>com.github.jaryur</groupId>
        <artifactId>spring-boot-starter-sequence-generator</artifactId>
        <version>{version}</version>
    </dependency>
    ```

2. application.properties
    
    * sequence properties
     ```
     sequence.application=default-application
     sequence.names=seq1,seq2
     sequence.step=300
     sequence.skip=0
     sequence.cacheNSteps=3
     sequence.spec.seq1=100,5
     ```
 
   * datasource properties
    
    ```
    sequence.datasource.url=jdbc:mysql://localhost:3306/sequence?useUnicode=true&characterEncoding=utf8
    &zeroDateTimeBehavior=convertToNull
    sequence.datasource.username=root
    sequence.datasource.password=
    sequence.datasource.driverClass=com.mysql.jdbc.Driver
        
    ```

3. create database **sequence**  on mysql

    * create tables
    ```
    
    CREATE TABLE `sequence` (
      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
      `application` varchar(64) NOT NULL DEFAULT 'default-application' COMMENT '应用名',
      `sequence_name` varchar(255) NOT NULL COMMENT '序列号名',
      `step` bigint(20) unsigned NOT NULL DEFAULT '300' COMMENT '每次递增步长',
      `min` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT '分区最小序号',
      `max` bigint(20) unsigned NOT NULL DEFAULT '4294967295' COMMENT '分区最大序号',
      `current_segment` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT '分区当前可用序号',
      `segment_unit` int(11) NOT NULL DEFAULT '1000000' COMMENT '每个分区包含序号数',
      `version` bigint(20) NOT NULL DEFAULT '1' COMMENT '自增版本号',
      PRIMARY KEY (`id`),
      UNIQUE KEY `idx_uniq_name` (`application`,`sequence_name`) USING BTREE
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Sequence表';
    
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
       
    
     ```
   * insert your sequence record
   
    ```
    insert into `sequence` ( `sequence_name`,`application`) values ('my-sequence-name','my-application-name');
    ```
    
4.  SequenceGenerator auto injection

**SingleSequenceGenerator**  or  **SequenceGenerator** 
