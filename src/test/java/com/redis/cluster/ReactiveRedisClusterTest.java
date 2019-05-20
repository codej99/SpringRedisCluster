package com.redis.cluster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReactiveRedisClusterTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 문자 데이터 구조 처리
     */
    @Test
    public void opsValue() {
        ReactiveValueOperations<String, String> valueOps = reactiveRedisTemplate.opsForValue();
        Set<String> cacheKeys = new HashSet<>();
        Map<String, String> setDatas = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            String key = "value_" + i;
            cacheKeys.add(key);
            setDatas.put(key, String.valueOf(i));
        }
        // previous key delete - sync
        redisTemplate.delete(cacheKeys);

        // async
        Mono<Boolean> results = valueOps.multiSet(setDatas);
        StepVerifier.create(results).expectNext(true).verifyComplete();

        Mono<List<String>> values = valueOps.multiGet(cacheKeys);
        StepVerifier.create(values)
                .expectNextMatches(x -> x.size() == 100).verifyComplete();
    }

    /**
     * List 데이터 구조 처리 - 순서 있음. value 중복 허용
     */
    @Test
    public void opsList() {
        ReactiveListOperations<String, String> listOps = reactiveRedisTemplate.opsForList();
        String cacheKey = "valueList";

        // previous key delete
        redisTemplate.delete(cacheKey);

        // async
        Mono<Long> results = listOps.leftPushAll(cacheKey, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        StepVerifier.create(results).expectNext(10L).verifyComplete();
        StepVerifier.create(reactiveRedisTemplate.type(cacheKey)).expectNext(DataType.LIST).verifyComplete();
        StepVerifier.create(listOps.size(cacheKey)).expectNext(10L).verifyComplete();
        StepVerifier.create(listOps.rightPop(cacheKey)).expectNext("0").verifyComplete();
        StepVerifier.create(listOps.leftPop(cacheKey)).expectNext("9").verifyComplete();
    }

    /**
     * Hash 데이터 구조 처리 - 순서 없음. key 중복허용 안함, value 중복 허용
     */
    @Test
    public void opsHash() {
        ReactiveHashOperations<String, String, String> hashOps = reactiveRedisTemplate.opsForHash();
        String cacheKey = "valueHash";
        Map<String, String> setDatas = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            setDatas.put("key_" + i, "value_" + i);
        }

        // previous key delete - sync
        redisTemplate.delete(cacheKey);

        // async
        StepVerifier.create(hashOps.putAll(cacheKey, setDatas)).expectNext(true).verifyComplete();
        StepVerifier.create(reactiveRedisTemplate.type(cacheKey)).expectNext(DataType.HASH).verifyComplete();
        StepVerifier.create(hashOps.size(cacheKey)).expectNext(10L).verifyComplete();
        StepVerifier.create(hashOps.get(cacheKey, "key_5")).expectNext("value_5").verifyComplete();
        StepVerifier.create(hashOps.remove(cacheKey, "key_5")).expectNext(1L).verifyComplete();
    }

    /**
     * Set 데이터 구조 처리 - 순서 없음, value 중복 허용 안함
     */
    @Test
    public void opsSet() {
        ReactiveSetOperations<String, String> setOps = reactiveRedisTemplate.opsForSet();
        String cacheKey = "valueSet";

        // previous key delete - sync
        redisTemplate.delete(cacheKey);

        // async
        for (int i = 0; i < 10; i++)
            StepVerifier.create(setOps.add(cacheKey, String.valueOf(i))).expectNext(1L).verifyComplete();

        StepVerifier.create(reactiveRedisTemplate.type(cacheKey)).expectNext(DataType.SET).verifyComplete();
        StepVerifier.create(setOps.size(cacheKey)).expectNext(10L).verifyComplete();
        StepVerifier.create(setOps.isMember(cacheKey, "5")).expectNext(true).verifyComplete();
    }

    /**
     * SortedSet 데이터 구조 처리 - 순서 있음, value 중복 허용 안함
     */
    @Test
    public void opsSortedSet() {
        ReactiveZSetOperations<String, String> zsetOps = reactiveRedisTemplate.opsForZSet();
        String cacheKey = "valueZSet";

        // previous key delete - sync
        redisTemplate.delete(cacheKey);

        // async
        for (int i = 0; i < 10; i++)
            StepVerifier.create(zsetOps.add(cacheKey, String.valueOf(i), i)).expectNext(true).verifyComplete();

        StepVerifier.create(reactiveRedisTemplate.type(cacheKey)).expectNext(DataType.ZSET).verifyComplete();
        StepVerifier.create(zsetOps.size(cacheKey)).expectNext(10L).verifyComplete();
        StepVerifier.create(zsetOps.reverseRank(cacheKey, "9")).expectNext(0L).verifyComplete();
    }

    /**
     * Geo 데이터 구조 처리 - 좌표 정보 처리, 타입은 zset으로 저장.
     */
    @Test
    public void opsGeo() {
        ReactiveGeoOperations<String, String> geoOps = reactiveRedisTemplate.opsForGeo();
        String[] cities = {"서울", "부산"};
        String[][] gu = {{"강남구", "서초구", "관악구", "동작구", "마포구"}, {"사하구", "해운대구", "영도구", "동래구", "수영구"}};
        String cacheKey = "valueGeo";

        // previous key delete - sync
        redisTemplate.delete(cacheKey);

        Map<String, Point> memberCoordiateMap = new HashMap<>();
        for (int x = 0; x < cities.length; x++) {
            for (int y = 0; y < 5; y++) {
                memberCoordiateMap.put(gu[x][y], new Point(x, y));
            }
        }
        // async
        StepVerifier.create(geoOps.add(cacheKey, memberCoordiateMap)).expectNext(10L).verifyComplete();
        StepVerifier.create(geoOps.distance(cacheKey, "강남구", "동작구")).expectNextMatches(x -> x.getValue() == 333678.8605).verifyComplete();
        StepVerifier.create(geoOps.position(cacheKey, "동작구")).expectNextMatches(x -> x.getX() == 0.000003 && x.getY() == 3.000001).verifyComplete();
    }

    /**
     * HyperLogLog 데이터 구조 처리 - 집합의 원소의 개수 추정, 타입은 string으로 저장.
     */
    @Test
    public void opsHyperLogLog() {
        ReactiveHyperLogLogOperations<String, String> hyperLogLogOps = reactiveRedisTemplate.opsForHyperLogLog();
        String cacheKey = "valueHyperLogLog";

        // previous key delete - sync
        redisTemplate.delete(cacheKey);

        // async
        String[] arr = {"1", "2", "2", "3", "4", "5", "5", "5", "5", "6", "7", "7", "7"};
        StepVerifier.create(hyperLogLogOps.add(cacheKey, arr)).expectNext(1L).verifyComplete();
        StepVerifier.create(hyperLogLogOps.size(cacheKey)).expectNext(7L).verifyComplete();
    }
}
