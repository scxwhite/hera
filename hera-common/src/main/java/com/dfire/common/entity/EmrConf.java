package com.dfire.common.entity;

import lombok.Builder;
import lombok.Data;

/**
 * desc:
 * emr 信息
 *
 * @author scx
 * @create 2019/03/18
 */

@Data
@Builder
public class EmrConf {
    private String loginURl;

    private String clusterName;

    private String masterInstanceType;

    private int numCoresNodes;

    private String coreInstanceType;

    private String emrManagedMasterSecurityGroup;

    private String emrManagedSlaveSecurityGroup;

    private String additionalMasterSecurityGroups;

    private String additionalSlaveSecurityGroups;

    private String serviceAccessSecurityGroup;

    private String ec2SubnetId;

}