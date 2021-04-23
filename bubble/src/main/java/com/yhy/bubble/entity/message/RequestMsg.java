package com.yhy.bubble.entity.message;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RequestMsg {

    private String message;

    private String toUserId;

    private String fromUserId;

    private int type;
}
