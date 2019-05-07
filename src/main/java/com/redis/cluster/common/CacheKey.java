package com.redis.cluster.common;

public class CacheKey {

    private CacheKey() {
    }

    public static final int DEFAULT_EXPIRE_SEC = 60;
    public static final String USER_SINGLE = "user-single";
    public static final int USER_SINGLE_EXPIRE_SEC = 180;
}
