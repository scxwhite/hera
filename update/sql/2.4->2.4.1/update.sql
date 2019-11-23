
#添加记录表
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
  AUTO_INCREMENT = 37
  DEFAULT CHARSET = utf8mb4 COMMENT ='日志记录表';

#添加逻辑删除字段
alter table hera_job add column `is_valid` tinyint(1) DEFAULT '1' COMMENT '任务是否删除';



