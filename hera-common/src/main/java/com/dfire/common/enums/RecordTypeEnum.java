package com.dfire.common.enums;

import java.util.Arrays;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/16
 */
public enum RecordTypeEnum {

    /**
     * 任务执行的操作
     */
    Execute("执行任务", 1),

    /**
     * 任务添加的操作
     */
    Add("添加任务", 2),
    /**
     * 调度中心脚本修改的操作
     */
    SCRIPT("更新脚本内容", 3),
    /**
     * 开启关闭任务的操作
     */
    SWITCH("任务开启/关闭状态", 4),

    RUN_TYPE("执行类型", 5),


    DEPEND("依赖关系", 6),

    CRON("定时表达式", 7),

    AREA("执行区域", 8),

    CONFIG("任务配置", 9),

    CANCEL("取消任务", 10),

    REMOTE("远程调用", 11),

    MOVE("移动任务/组", 12),

    DELETE("删除任务/组", 13),

    LOGIN("登录", 14),

    UPLOAD("上传", 15),

    /**
     * 除之上的操作的所有操作
     */
    Other("其它操作", 0);


    private String type;

    private Integer id;


    RecordTypeEnum(String type, Integer id) {
        this.type = type;
        this.id = id;
    }


    public static RecordTypeEnum parseById(Integer id) {
        return Arrays.stream(RecordTypeEnum.values()).filter(record -> record.id.equals(id)).findAny().orElse(null);
    }

    public static RecordTypeEnum parseByName(String name) {
        return Arrays.stream(RecordTypeEnum.values()).filter(record -> record.type.equals(name)).findAny().orElse(null);
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}