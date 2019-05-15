package com.redis.cluster.controller;

import com.redis.cluster.pubsub.RedisPublisher;
import com.redis.cluster.pubsub.RedisSubscriber;
import com.redis.cluster.pubsub.RoomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RequestMapping("/pubsub")
@RestController
public class PubSubController {
    // topic에 발행되는 액션을 처리할 Listner
    private final RedisMessageListenerContainer redisMessageListener;
    // 발행자
    private final RedisPublisher redisPublisher;
    // 구독자
    private final RedisSubscriber redisSubscriber;
    // 특정 topic에 메시지를 발송할 수 있도록 topic정보를 Map에 저장
    private Map<String, ChannelTopic> channels;

    @PostConstruct
    public void init() {
        // 실행될때 topic정보를 담을 Map을 초기화
        channels = new HashMap<>();
    }

    // 유효한 Topic 리스트 반환
    @GetMapping("/room")
    public Set<String> findAllRoom() {
        return channels.keySet();
    }

    // Topic 생성하여 Listener에 등록후 Topic Map에 저장
    @PutMapping("/room/{roomId}")
    public void createRoom(@PathVariable String roomId) {
        ChannelTopic channel = new ChannelTopic(roomId);
        redisMessageListener.addMessageListener(redisSubscriber, channel);
        channels.put(roomId, channel);
    }

    // 특정 Topic에 메시지 발송
    @PostMapping("/room/{roomId}")
    public void pushMessage(@PathVariable String roomId, @RequestParam String name, @RequestParam String message) {
        ChannelTopic channel = channels.get(roomId);
        redisPublisher.publish(channel, RoomMessage.builder().name(name).roomId(roomId).message(message).build());
    }

    // 특정 Topic 삭제 후 Listener 해제
    @DeleteMapping("/room/{roomId}")
    public void deleteRoom(@PathVariable String roomId) {
        ChannelTopic channel = channels.get(roomId);
        redisMessageListener.removeMessageListener(redisSubscriber, channel);
        channels.remove(roomId);
    }
}