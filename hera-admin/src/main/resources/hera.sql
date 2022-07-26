SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


CREATE TABLE IF NOT EXISTS `hera_action`
(
  `id`                   bigint(20)   NOT NULL COMMENT '任务对应的唯一18位数字版本号',
  `job_id`               bigint(20)   NOT NULL COMMENT '版本对应的任务id',
  `auto`                 tinyint(2)                   DEFAULT NULL,
  `configs`              text COMMENT '任务的配置的变量',
  `cron_expression`      varchar(256)                 DEFAULT NULL COMMENT '当前版本对应的cron表达式',
  `cycle`                varchar(256)                 DEFAULT NULL COMMENT '是否为循环任务',
  `dependencies`         text COMMENT '依赖任务的版本号，逗号分隔',
  `job_dependencies`     varchar(2048)                DEFAULT NULL COMMENT '依赖任务的id,逗号分隔',
  `description`          varchar(256)                 DEFAULT NULL COMMENT '版本描述',
  `gmt_create`           datetime                     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`         datetime                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_id`             int(11)      NOT NULL COMMENT '版本可运行分发的机器组',
  `history_id`           bigint(20)                   DEFAULT NULL COMMENT '当前版本运行的history id',
  `host`                 varchar(32)                  DEFAULT NULL COMMENT '执行机器ip ',
  `last_end_time`        datetime                     DEFAULT NULL,
  `last_result`          varchar(256)                 DEFAULT NULL,
  `name`                 varchar(256) NOT NULL        DEFAULT '' COMMENT '任务描述',
  `offset`               tinyint(2) unsigned zerofill DEFAULT NULL,
  `owner`                varchar(32)  NOT NULL COMMENT '任务的owner',
  `post_processors`      varchar(256)                 DEFAULT NULL,
  `pre_processors`       varchar(256)                 DEFAULT NULL,
  `ready_dependency`     text COMMENT '上游任务已完成的版本号',
  `resources`            text COMMENT '任务上传的资源配置',
  `run_type`             varchar(16)                  DEFAULT NULL COMMENT '任务触发类型(shell, hive)',
  `schedule_type`        tinyint(2)                   DEFAULT NULL COMMENT '任务调度类型(1,依赖调度，2，被依赖调度)',
  `script`               mediumtext COMMENT '任务对应的脚本',
  `start_time`           bigint(20)                   DEFAULT NULL,
  `start_timestamp`      bigint(20)                   DEFAULT NULL,
  `statistic_end_time`   datetime                     DEFAULT NULL,
  `statistic_start_time` datetime                     DEFAULT NULL,
  `status`               varchar(16)                  DEFAULT NULL COMMENT '当前版本的运行状态，job_history完成后，会写更新此状态',
  `timezone`             varchar(32)                  DEFAULT NULL,
  `host_group_id`        tinyint(2)                   DEFAULT NULL COMMENT '任务可分配的执行服务器组',
  `down_actions`         varchar(16)                  DEFAULT NULL,
  `batch_id` varchar(50) DEFAULT NULL COMMENT '批次号',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_job_id` (`job_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='job实例记录表';

CREATE TABLE IF NOT EXISTS `hera_action_history`
(
  `id`                 bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id`             bigint(20)    DEFAULT NULL COMMENT 'hera任务id',
  `action_id`          bigint(20)    DEFAULT NULL COMMENT '任务对应的版本号，18位整数',
  `cycle`              varchar(16)   DEFAULT NULL COMMENT '是否是循环任务',
  `end_time`           datetime      DEFAULT NULL COMMENT '任务执行结束时间',
  `execute_host`       varchar(32)   DEFAULT NULL COMMENT '当前版本任务执行的服务器',
  `gmt_create`         datetime      DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`       datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `illustrate`         varchar(256)  DEFAULT NULL COMMENT '任务运行描述',
  `log`                longtext COMMENT '任务运行日志',
  `operator`           varchar(32)   DEFAULT NULL COMMENT '任务运行操作人',
  `properties`         varchar(6144) DEFAULT NULL,
  `start_time`         datetime      DEFAULT NULL COMMENT '任务开始执行的时间',
  `statistic_end_time` datetime      DEFAULT NULL COMMENT '版本生成结束时间',
  `status`             varchar(16)   DEFAULT NULL COMMENT '当前版本的任务运行状态',
  `timezone`           varchar(32)   DEFAULT NULL,
  `trigger_type`       tinyint(4)    DEFAULT NULL COMMENT '任务触发类型(1,自动调度,2,手动触发,3,手动恢复)',
  `host_group_id`      int(11)       DEFAULT NULL COMMENT '任务可分配的执行服务器组',
  batch_id varchar(50) DEFAULT NULL COMMENT '批次号',
  biz_label varchar(500) DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`),
  KEY `idx_action_id_job_id` (`action_id`, `job_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_start_time` (`start_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Job运行日志表';
CREATE TABLE IF NOT EXISTS `hera_advice`
(
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `msg`         varchar(256) DEFAULT NULL COMMENT '消息',
  `address`     varchar(256) DEFAULT NULL COMMENT 'ip地址',
  `color`       varchar(7)   DEFAULT NULL COMMENT '颜色',
  `create_time` varchar(19)  DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='hera建议表';


CREATE TABLE IF NOT EXISTS `hera_area`
(
  `id`           int(11) NOT NULL AUTO_INCREMENT COMMENT '区域id',
  `name`         varchar(50) DEFAULT NULL COMMENT '区域名',
  `timezone`     varchar(25) DEFAULT NULL COMMENT '时区',
  `gmt_create`   datetime    DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='hera任务区域表';

CREATE TABLE IF NOT EXISTS `hera_debug_history`
(
  `id`            bigint(20) NOT NULL AUTO_INCREMENT,
  `end_time`      datetime     DEFAULT NULL COMMENT '运行结束时间',
  `execute_host`  varchar(255) DEFAULT NULL COMMENT '执行服务器',
  `file_id`       bigint(20)   DEFAULT NULL COMMENT '脚本文件id',
  `gmt_create`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '运行日志创建时间',
  `gmt_modified`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '运行日志修改时间',
  `log`           longtext COMMENT '脚本运行日志',
  `run_type`      varchar(16)  DEFAULT NULL COMMENT '运行类型（hive,shell）',
  `script`        longtext COMMENT '完整运行脚本，',
  `start_time`    datetime     DEFAULT NULL COMMENT '统计开始时间',
  `status`        varchar(32)  DEFAULT NULL COMMENT '脚本运行状态(runnin,success,failed,wait)',
  `owner`         varchar(32)  DEFAULT NULL COMMENT '脚本owner',
  `host_group_id` tinyint(4)   DEFAULT NULL COMMENT '执行机器组id',
  `job_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联调度任务id',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = COMPACT COMMENT ='开发中心脚本运行日志表';

CREATE TABLE IF NOT EXISTS `hera_file`
(
  `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
  `content`       mediumtext COMMENT '脚本文件内容',
  `gmt_create`    datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '脚本文件创建时间',
  `gmt_modified`  datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `name`          varchar(128) NOT NULL COMMENT '脚本名称',
  `owner`         varchar(32)  NOT NULL COMMENT '脚本的owner',
  `parent`        int(20)    DEFAULT NULL COMMENT '父目录id',
  `type`          tinyint(4)   NOT NULL COMMENT '文件类型(1,目录,2,文件)',
  `host_group_id` tinyint(2) DEFAULT NULL COMMENT '执行机器组id',
  `job_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联调度任务id',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开发中心脚本记录表';

CREATE TABLE IF NOT EXISTS `hera_group`
(
  `id`           int(11)      NOT NULL AUTO_INCREMENT,
  `configs`      text,
  `description`  varchar(256)          DEFAULT NULL,
  `directory`    int(11)      NOT NULL,
  `gmt_create`   datetime              DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `name`         varchar(255) NOT NULL,
  `owner`        varchar(255) NOT NULL,
  `parent`       int(11)               DEFAULT NULL,
  `resources`    text,
  `existed`      int(11)      NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='调度中心目录表';

CREATE TABLE IF NOT EXISTS `hera_host_group`
(
  `id`           int(11) NOT NULL AUTO_INCREMENT,
  `name`         varchar(128) DEFAULT NULL COMMENT '组描述',
  `effective`    tinyint(2)   DEFAULT '0' COMMENT '是否有效（1，有效，0，无效）',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `description`  varchar(256) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='机器组记录表';

CREATE TABLE IF NOT EXISTS `hera_host_relation`
(
  `id`            int(11) NOT NULL AUTO_INCREMENT,
  `host`          varchar(32) DEFAULT NULL COMMENT '机器ip',
  `host_group_id` int(11)     DEFAULT NULL COMMENT '机器所在组id',
  `domain`        varchar(16) DEFAULT '' COMMENT '机器域名',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='机器与机器组关联表';


CREATE TABLE IF NOT EXISTS `hera_job`
(
  `id`                   bigint(30)   NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `auto`                 tinyint(2)    DEFAULT '0' COMMENT '自动调度是否开启',
  `configs`              text COMMENT '配置的环境变量',
  `cron_expression`      varchar(32)   DEFAULT NULL COMMENT 'cron表达式',
  `cycle`                varchar(16)   DEFAULT NULL COMMENT '是否是循环任务',
  `dependencies`         varchar(2000) DEFAULT NULL COMMENT '依赖的任务id,逗号分隔',
  `description`          varchar(256)  DEFAULT NULL COMMENT '任务描述',
  `gmt_create`           datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified`         datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_id`             int(11)      NOT NULL COMMENT '所在的目录 id',
  `history_id`           bigint(20)    DEFAULT NULL COMMENT '运行历史id',
  `host`                 varchar(32)   DEFAULT NULL COMMENT '运行服务器ip',
  `last_end_time`        datetime      DEFAULT NULL,
  `last_result`          varchar(16)   DEFAULT NULL,
  `name`                 varchar(256) NOT NULL COMMENT '任务名称',
  `offset`               int(11)       DEFAULT NULL,
  `owner`                varchar(256) NOT NULL,
  `post_processors`      varchar(256)  DEFAULT NULL COMMENT '任务运行所需的后置处理',
  `pre_processors`       varchar(256)  DEFAULT NULL COMMENT '任务运行所需的前置处理',
  `ready_dependency`     varchar(16)   DEFAULT NULL COMMENT '任务已完成的依赖',
  `resources`            text COMMENT '上传的资源文件配置',
  `run_type`             varchar(16)   DEFAULT NULL COMMENT '运行的job类型(hive,shell)',
  `schedule_type`        tinyint(4)    DEFAULT NULL COMMENT '任务调度类型',
  `script`               mediumtext COMMENT '脚本内容',
  `start_time`           datetime      DEFAULT NULL,
  `start_timestamp`      bigint(20)    DEFAULT NULL,
  `statistic_end_time`   datetime      DEFAULT NULL,
  `statistic_start_time` datetime      DEFAULT NULL,
  `status`               varchar(16)   DEFAULT NULL,
  `timezone`             varchar(32)   DEFAULT NULL,
  `host_group_id`        tinyint(2)    DEFAULT NULL COMMENT '分发的执行机器组id',
  `must_end_minute`      int(2)        DEFAULT '0',
  `area_id`              varchar(50)   DEFAULT '1' COMMENT '区域ID,多个用,分割',
  `repeat_run`           tinyint(2)    DEFAULT '1' COMMENT '是否允许任务重复执行',
  `is_valid` tinyint(1) DEFAULT '1' COMMENT '任务是否删除',
  cron_period varchar(100) DEFAULT NULL,
  cron_interval int(11) DEFAULT NULL,
  biz_label varchar(500) DEFAULT '',
  `estimated_end_hour` int(4) NOT NULL DEFAULT '0' COMMENT '预计结束结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='调度中心任务记录表';

CREATE TABLE IF NOT EXISTS `hera_job_monitor`
(
  `job_id`   bigint(20)   NOT NULL,
  `user_ids` varchar(100) NOT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务关注人列表';

CREATE TABLE IF NOT EXISTS `hera_lock`
(
  `id`            int(11) NOT NULL AUTO_INCREMENT,
  `gmt_create`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified`  datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `host`          varchar(32) DEFAULT NULL COMMENT '机器对应ip',
  `server_update` datetime    DEFAULT NULL COMMENT '心跳更新时间',
  `subgroup`      varchar(32) DEFAULT NULL COMMENT '机器所在组，',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='分布式锁记录表';

CREATE TABLE IF NOT EXISTS `hera_permission`
(
  `id`           bigint(20) NOT NULL AUTO_INCREMENT,
  `gmt_create`   datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `target_id`    bigint(20)          DEFAULT NULL COMMENT '授权的任务或者组id',
  `type`         varchar(32)         DEFAULT NULL COMMENT '授权类型(job或者group)',
  `uid`          varchar(32)         DEFAULT NULL COMMENT '被授权着名称',
  `is_valid`     tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务授权记录表';




CREATE TABLE IF NOT EXISTS `hera_user`
(
  `id`           bigint(20) NOT NULL AUTO_INCREMENT,
  `email`        varchar(255)  DEFAULT NULL,
  `gmt_create`   datetime      DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `name`         varchar(255)  DEFAULT NULL,
  `phone`        varchar(2000) DEFAULT NULL,
  `uid`          varchar(255)  DEFAULT NULL,
  `wangwang`     varchar(255)  DEFAULT NULL,
  `password`     varchar(255)  DEFAULT NULL,
  `user_type`    int(11)       DEFAULT '0',
  `is_effective` int(11)       DEFAULT '0',
  `description`  varchar(256)  DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='用户组表';


CREATE TABLE IF NOT EXISTS `hera_sso`
(
  `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         varchar(16)      NOT NULL DEFAULT '' COMMENT '用户名',
  `password`     varchar(32)      NOT NULL DEFAULT '' COMMENT '密码',
  `gid`          int(11)          NOT NULL DEFAULT '0' COMMENT '组id，对应hera_user的主键',
  `phone`        char(11)         NOT NULL DEFAULT '' COMMENT '手机号',
  `email`        varchar(52)      NOT NULL DEFAULT '' COMMENT '邮箱',
  `job_number`   char(5)          NOT NULL DEFAULT '' COMMENT '工号',
  `gmt_modified` bigint(20)       NOT NULL DEFAULT '0' COMMENT '编辑时间',
  `is_valid`     tinyint(1)       NOT NULL DEFAULT '0' COMMENT '是否有效：0:无效：1：有效',
  `gmt_create`   datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_name` (`name`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='sso用户表';

CREATE TABLE IF NOT EXISTS `hera_record`
(
  `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type`         tinyint(4)       NOT NULL DEFAULT '0' COMMENT '日志类型：比如编辑、更新',
  `content`      mediumtext COMMENT '脚本的变更前的内容',
  `log_type`     varchar(15)      NOT NULL DEFAULT '-1' COMMENT '记录的日志的类型 比如：任务/组',
  `log_id`       int(11)          NOT NULL DEFAULT '-1' COMMENT '任务id/组id',
  `gmt_create`   bigint(13)       NOT NULL DEFAULT '1557814087800' COMMENT '创建时间',
  `gmt_modified` bigint(13)       NOT NULL DEFAULT '1557814087800' COMMENT '同步专用字段',
  `sso`          varchar(32)      NOT NULL DEFAULT 'hera' COMMENT '用户名称',
  `gId`          int(11)          NOT NULL DEFAULT '-1' COMMENT '组id',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='日志记录表';


CREATE TABLE `hera_rerun` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(256) NOT NULL DEFAULT '' COMMENT '重跑名称',
  `start_millis` bigint(13) NOT NULL DEFAULT '0' COMMENT '其实跑的日期',
  `end_millis` bigint(13) NOT NULL COMMENT '结束跑的日期',
  `sso_name` varchar(16) NOT NULL COMMENT '创建人',
  `extra` varchar(1000) NOT NULL DEFAULT '' COMMENT '其它配置',
  `gmt_create` bigint(13) NOT NULL DEFAULT '0' COMMENT '创建时间',
  `gmt_modified` bigint(13) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `job_id` int(11) NOT NULL DEFAULT '0' COMMENT '任务ID',
  `is_end` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否结束',
  `action_now` varchar(18) NOT NULL DEFAULT '' COMMENT '当前执行的版本号',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='hera重跑任务表';

BEGIN;
## 添加默认用户组

insert into hera_user (email, name, uid, password, user_type, is_effective)
values ('1142819049@qq.com', 'hera', 'hera', 'd3886bd3bcba3d88e2ab14ba8c9326da', 0, 1);


## 添加默认用户
insert into hera_sso (name,password,gid,email,is_valid) values('hera','d3886bd3bcba3d88e2ab14ba8c9326da',1,'1142819049@qq.com',1);
## 添加默认区域
insert into hera_area (name)
values ('all');

## 初始化任务目录
INSERT INTO `hera_group`
VALUES ('1', '{\"name\":\"赫拉分布式任务调度系统\"}', '', '0', '2018-12-21 15:11:39', '2018-12-28 10:46:47', 'hera分布式调度系统', 'hera',
        '0', '[]', '1'),
       ('2', '{\"qq\":\"1142819049\"}', '', '1', '2018-12-21 15:15:36', '2018-12-21 15:31:08', 'test', 'hera', '1',
        '[]', '1');

## 添加初始化机器组
insert into hera_host_group (id, name, effective, description)
values (1, '默认组', 1, '机器默认组');
insert into hera_host_group (id, name, effective, description)
values (2, 'spark组', 1, '执行spark任务');
## 添加初始化任务
INSERT INTO `hera_job`
VALUES ('1', '0',
        '{\"run.priority.level\":\"1\",\"roll.back.wait.time\":\"1\",\"roll.back.times\":\"0\",\"qqGroup\":\"965839395\"}',
        '0 0 3 * * ?', null, '', '输出测试', '2018-12-22 11:14:55', '2019-01-04 11:14:09', '2', null, null, null, null,
        'echoTest', null, 'hera', null, null, null, null, 'shell', '0',
        'echo ${name}\n\necho \"当前时间戳\":${zdt.getTime()}\necho \"     明天\":${zdt.addDay(1).format(\"yyyy-MM-dd HH:mm:ss\")}\n\necho \"上个月的今天\": ${zdt.add(2,-1).format(\"yyyy-MM-dd HH:mm:ss\")}\n\necho \"真实的今天\":${zdt.getToday()}\n\n\necho \"如果需要更多时间查看HeraDateTool类,可以自定义时间\"\n\n\necho ${qqGroup}',
        null, null, null, null, null, null, '1', null, '1', 0,1 ,'day', -1 ,'数据层,XXX业务','0');
## 初始化开发中心文档
INSERT INTO `hera_file`
VALUES ('1', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '个人文档', 'hera', null, '1', '0',0);
INSERT INTO `hera_file`
VALUES ('2', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '共享文档', 'all', null, '1', '0',0);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

