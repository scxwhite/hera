package com.dfire.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:51 2018/4/17
 * @desc
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeraGroupVo {

    private Integer id;
    private String name;
    private String owner;
    private String description;
    private String configs;


    private Map<String, String> properties;
    private List<Map<String,String>> resources;
}
