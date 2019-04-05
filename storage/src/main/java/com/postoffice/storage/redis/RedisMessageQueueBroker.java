package com.postoffice.storage.redis;

import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.mongo.Message;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisMessageQueueBroker implements MessageQueueBroker {

    public final static String CONCRETE_PREFIX_KEY = "MSG_PO_";

    ReactiveRedisTemplate<String, Message> reactiveRedis;
    /*BiFunction<String,Message,Mono<Long>> push;
    Function<String,Mono<Message>> pop;
    BiFunction<String,Duration,Mono<Message>> popDuration;*/
    private final int EARLIEST_INDEX;

    public RedisMessageQueueBroker(ReactiveRedisTemplate<String, Message> reactiveRedis) {
        this.reactiveRedis = reactiveRedis;
        EARLIEST_INDEX = -1;//与 push pop 的方向有关
    }

    @Override
    public Mono<Long> pushMessage(Message message){
        return reactiveRedis.opsForList().leftPush(getKey(message.getDomainID(),message.getReceiver()), message);
    }

    @Override
    public Mono<Message> popMessage(String domainID,String user){
        return reactiveRedis.opsForList().rightPop(getKey(domainID,user));
    }

    @Override
    public Flux<Message> popMessage(String domainID,String user,Duration duration){
        //return pop(getKey(domainID,user));
        return null;
    }

    @Override
    public Mono<Message> earliestMessage(String domainID, String user) {
        return reactiveRedis.opsForList().index(getKey(domainID,user),EARLIEST_INDEX);
    }


    private static String getKey(String domainID,String user){
        return CONCRETE_PREFIX_KEY + "/D:"+domainID+"/U:"+user;
    }
}
