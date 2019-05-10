package com.redis.cluster.controller;

import com.redis.cluster.common.CacheKey;
import com.redis.cluster.entity.User;
import com.redis.cluster.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/redis")
@RestController
public class RedisController {

    private final UserJpaRepo userJpaRepo;

    @Cacheable(value = CacheKey.USER, key = "#msrl", unless = "#result == null")
    @GetMapping("/user/{msrl}")
    public User findOne(@PathVariable long msrl) {
        return userJpaRepo.findById(msrl).orElse(null);
    }

    @PostMapping("/user")
    @ResponseBody
    public User postUser(@RequestBody User user) {
        return userJpaRepo.save(user);
    }

    @CachePut(value = CacheKey.USER, key = "#user.msrl")
    @PutMapping("/user")
    @ResponseBody
    public User putUser(@RequestBody User user) {
        return userJpaRepo.save(user);
    }

    @CacheEvict(value = CacheKey.USER, key = "#msrl")
    @DeleteMapping("/user/{msrl}")
    @ResponseBody
    public boolean deleteUser(@PathVariable long msrl) {
        userJpaRepo.deleteById(msrl);
        return true;
    }
}
