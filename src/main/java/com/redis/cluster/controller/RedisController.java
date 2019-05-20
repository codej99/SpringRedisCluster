package com.redis.cluster.controller;

import com.redis.cluster.entity.User;
import com.redis.cluster.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/redis")
@RestController
public class RedisController {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserJpaRepo userJpaRepo;

    @GetMapping("/ops/value")
    public List<String> redisClusterTest() {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        Collection<String> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add("valueOps_" + i);
            valueOps.set("valueOps_" + i, String.valueOf(i));
        }
        return valueOps.multiGet(keys);
    }

    @PostMapping("/post/user")
    public void redisClusterPostUser() {
        userJpaRepo.save(User.builder()
                .uid("strongdaddy@naver.com")
                .password("1234")
                .name("strongdaddy")
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
    }
}
