package com.dfire.common.entity;

import com.dfire.common.config.SkipColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:34 2018/1/11
 * @desc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraPermission {


    private int id;

    private String type;

    private Long targetId;

    private String uid;

    @SkipColumn
    private Date gmtCreate;

    @SkipColumn
    private Date gmtModified;

    private Integer isValid;


}
