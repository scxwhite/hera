package com.dfire.graph;

import lombok.Data;

@Data
public class JobRelation {

    private Integer id;

    private String name;

    private String dependencies;

    private Integer pid;

    private String pname;

    private Integer auto;

    private Integer pAuto;


}
