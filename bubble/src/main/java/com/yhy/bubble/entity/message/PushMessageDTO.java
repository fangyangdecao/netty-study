package com.yhy.bubble.entity.message;

import lombok.*;

import java.io.Serializable;


@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    private String userId;

    private String fromUserId;

}
