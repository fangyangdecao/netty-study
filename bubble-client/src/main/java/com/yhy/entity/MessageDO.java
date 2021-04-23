package com.yhy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDO {
    private String msg;
    private Integer from;
    private Integer to;
    private Integer type;
}
