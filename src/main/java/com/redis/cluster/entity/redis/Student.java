package com.redis.cluster.entity.redis;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@RedisHash("student")
public class Student {
    @Id
    private long studentId;
    private String name;

    public void update(String name) {
        this.name = name;
    }
}
