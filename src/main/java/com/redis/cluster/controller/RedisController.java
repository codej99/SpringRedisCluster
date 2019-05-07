package com.redis.cluster.controller;

import com.redis.cluster.common.CacheKey;
import com.redis.cluster.entity.User;
import com.redis.cluster.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RequiredArgsConstructor
@RequestMapping("/redis")
@RestController
public class RedisController {

    private final UserJpaRepo userJpaRepo;
    private static final String UID = "strongdaddy@naver.com";

    @Cacheable(value = CacheKey.USER_SINGLE, key = "#uid", unless = "#result == null")
    @GetMapping("/user/{uid}")
    public User findOne(@PathVariable String uid) {
        return userJpaRepo.findByUid(uid).orElse(null);
    }

    @PostMapping("/user")
    @ResponseBody
    public User postUser() {
        userJpaRepo.save(User.builder()
                .uid(UID)
                .password("1234")
                .name("strongdaddy")
                .roles(Collections.singletonList("ROLE_USER"))
                .build());

        return userJpaRepo.findByUid(UID).orElse(null);
    }

    @CachePut(value = CacheKey.USER_SINGLE, key = "#user.uid")
    @PutMapping("/user")
    @ResponseBody
    public User putUser(@RequestBody User user) {
        userJpaRepo.save(user);
        return userJpaRepo.findByUid(UID).orElse(null);
    }
}
