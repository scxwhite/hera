package com.dfire.common.vo;

import lombok.Data;

/**
 * @author xiaosuda
 * @date 2018/11/14
 */
@Data
public class MachineInfoVo {
    /**
     * 文件系统分区
     */
    private String filesystem;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 分区大小
     */
    private String size;
    /**
     * 分区已经使用大小
     */
    private String used;
    /**
     * 分区可用大小
     */
    private String avail;
    /**
     * 分区使用的比例
     */
    private String use;
    /**
     * 磁盘分区挂载的目录 即挂载点
     */
    private String mountedOn;
}
