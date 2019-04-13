package com.postoffice.storage.redis;

import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisQueueBroker implements MessageQueueBroker {

    public final static String CONCRETE_PREFIX_KEY = "MSG_PO_";

    ReactiveRedisTemplate<String, MessageDBEntity> reactiveRedis;
    /*BiFunction<String,MessageDBEntity,Mono<Long>> push;
    Function<String,Mono<MessageDBEntity>> pop;
    BiFunction<String,Duration,Mono<MessageDBEntity>> popDuration;*/
    private final int EARLIEST_INDEX;

    public RedisQueueBroker(ReactiveRedisTemplate<String, MessageDBEntity> reactiveRedis) {
        this.reactiveRedis = reactiveRedis;
        EARLIEST_INDEX = -1;//与 push pop 的方向有关
    }

    @Override
    public Mono<MessageDBEntity> pushMessage(MessageDBEntity messageDBEntity, boolean bothQueuesOfDeliverAndReceiver){
        Mono<MessageDBEntity> messageMono = reactiveRedis.opsForList().leftPush(getKey(messageDBEntity.getDomainID(), messageDBEntity.getReceiver()), messageDBEntity)
                .map(l-> messageDBEntity);
        if(bothQueuesOfDeliverAndReceiver){
            return messageMono.flatMap(afterReceive -> reactiveRedis.opsForList().leftPush(getKey(afterReceive.getDomainID(),afterReceive.getDeliver()),afterReceive)
            .map(l -> messageDBEntity));
        }
        return messageMono;
    }

    @Override
    public Mono<MessageDBEntity> popMessage(String domainID, String user){
        return reactiveRedis.opsForList().rightPop(getKey(domainID,user));
    }

    @Override
    public Flux<MessageDBEntity> popMessage(String domainID, String user, Duration duration){
        //return pop(getKey(domainID,user));
        return null;
    }

    @Override
    public Mono<MessageDBEntity> earliestMessage(String domainID, String user) {
        return reactiveRedis.opsForList().index(getKey(domainID,user),EARLIEST_INDEX);
    }


    private static String getKey(String domainID,String user){
        return CONCRETE_PREFIX_KEY + "/D:"+domainID+"/U:"+user;
    }
}
