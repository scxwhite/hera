#权限表逻辑移除
alter table hera_permission add columns `is_valid` tinyint(1) NOT NULL DEFAULT '1';

#增加sso用户表
CREATE TABLE if not exists `hera_sso`
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
