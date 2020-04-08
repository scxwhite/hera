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


alter table hera_job add column `estimated_end_hour` int(4) NOT NULL DEFAULT '0' COMMENT '预计结束结束时间';

alter table hera_file add column `job_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联调度任务id';

alter table hera_debug_history add column `job_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联调度任务id';
