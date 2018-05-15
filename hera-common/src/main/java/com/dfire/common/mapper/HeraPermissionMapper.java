package com.dfire.common.mapper;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:49 2018/5/15
 * @desc
 */
public interface HeraPermissionMapper {

    Boolean hasGroupPermission(String user,String groupId);

    Boolean hasJobPermission(String user,String jobId);

    Boolean hasActionPermission(String user,String jobId);
    /**
     * 添加组的管理员
     * @param user 被授权人
     */
    void addGroupAdmin(String user,String groupId);
    /**
     * 删除组管理员
     * @param user 被授权人
     */
    void removeGroupAdmin(String user,String groupId);

    void addJobAdmin(String user,String jobId) ;

    void removeJobAdmin(String user,String jobId);
    /**
     * 该组的管理员名单
     * @param groupId
     * @return
     */
    List<String> getGroupAdmins(String groupId);
    /**
     * 该Job的管理员名单
     * @param jobId
     * @return
     */
    List<String> getJobAdmins(String jobId);
    /**
     * 该Job的同系列任务
     * @param jobId
     * @return
     */
    List<Long> getJobACtion(String jobId);
}
