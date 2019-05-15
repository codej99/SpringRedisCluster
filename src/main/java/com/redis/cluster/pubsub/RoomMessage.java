package com.redis.cluster.pubsub;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoomMessage implements Serializable {
    private static final long serialVersionUID = 2082503192322391880L;
    private String roomId;
    private String name;
    private String message;
}
