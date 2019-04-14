# Introduction
 基于Mysql的自增sequence生成器

# Quick Start 

1. 引入Maven依赖

    ```
    <dependency>
        <groupId>com.github.jaryur</groupId>
        <artifactId>spring-boot-starter-sequence-generator</artifactId>
        <version>{version}</version>
    </dependency>
    ```

2. application.properties配置
    
    * sequence配置
     ```
     #应用名
     sequence.application=default-application
     #sequence名，可以指定多个,逗号分割
     sequence.names=seq1,seq2
     #每次获取的序号数
     sequence.step=300
     #每次获取Sequence跳过的个数
     #sequence.skip=0
     #缓存cacheNSteps个序号段
     sequence.cacheNSteps=3
     #指定seq1的step和cacheNSteps
     sequence.spec.seq1=100,5
     ```
 
 > step代表每次取出的一段sequence的大小写,cacheNSteps代表缓存的sequence个数，详见【原理】部分说明
     
   * 数据源配置
    
        ```
        #sequence数据源
        sequence.datasource.url=jdbc:mysql://localhost:3306/sequence?useUnicode=true&characterEncoding=utf8
        &zeroDateTimeBehavior=convertToNull
        sequence.datasource.username=root
        sequence.datasource.password=
        sequence.datasource.driverClass=com.mysql.jdbc.Driver
       ```

3. 新增对应的sequence记录

    * 创建表
    ```
    
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
   * 新增sequence记录
    ```
    insert into `sequence` ( `min`, `max`, `segment_unit`, `sequence_name`,`application`) 
    values ( '1' , '4294967295', '1000000', 'sequence名','应用名');
    ```
    
4. 通过Spring注入Bean

* 只配置一个sequence, 通过Spring注入 **private SingleSequenceGenerator sequenceGenerator** ,并调用**getNextInt**或**getNextLong**方法
* 配置了多个sequence, 通过Spring注入 **private SequenceGenerator sequenceGenerator** ,并调用**getNextInt**或**getNextLong**方法
* 批量获取一批序号段，调用**getRange**方法（此时skip参数不生效）

> 默认超时时间1S

# 原理 
基于MySql表，每次预先获取step个序号，更新数据库current字段为current + step，每次获取序号优先从内存中序号段获取，如果内存中序号段已经用完，则从数据库获取下一个序号段
cacheNSteps参数表示提前取出序号段并进行本地缓存（缓存 cacheNSteps*step 个序号），自动异步补充，减少序号段消耗完时访问数据库的耗时，提升性能