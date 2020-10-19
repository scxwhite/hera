package com.dfire.monitor.domain;

import lombok.*;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlarmInfo {

    private String message;
    private String userId;
    private String phone;
}