package com.postoffice.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.entity.FriendsRelationDBEntity;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Date;

@Service
public class MessageService {

    private Logger logger = Loggers.getLogger(MessageService.class);

    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedis;

    @Autowired
    MessageStorageBroker messageStorageBroker;

    @Autowired
    MessageQueueBroker messageQueueBroker;

    @Autowired
    FriendsRelationService friendsRelationService;

    @Autowired
    SecurityHandler securityHandler;


    public Mono<MessageDBEntity> delivery(
            @PathVariable String token,
            @PathVariable String receiver,
            @RequestBody String content) {
        Mono<AuthInfo> authInfoMono = securityHandler.validateToken(token);

        MessageDBEntity messageDBEntity = new MessageDBEntity();
        messageDBEntity.setGenerateTime(new Date());
        messageDBEntity.setReceiver(receiver);
        messageDBEntity.setDeliveryTime(new Date());
        messageDBEntity.setMessageType(ECommerceChartMessage.MessageTypeEnum.UNKNOWN);
        messageDBEntity.setMessageDirection(ECommerceChartMessage.MessageDirectionEnum.UNKNOWN);
        messageDBEntity.setContent(content);

        //relationMono 存储的值：
        //UserA:Deliver
        //UserB:Receiver
        Mono<FriendsRelationDBEntity> relationMono =
                authInfoMono
                        //Receiver->Deliver
                        .flatMap(authInfo -> messageStorageBroker.updateOrSaveFriend(authInfo.getDomain(), receiver, authInfo.getUser().getId()))
                        //Deliver->Receiver
                        .flatMap(relation -> messageStorageBroker.updateOrSaveFriend(relation.getDomainID(), relation.getFriendID(), relation.getPersonID()));
        //方案一，zip with friends relation
        return Mono.just(messageDBEntity)
                .log(logger)
                /*Token得到的Auth信息 zip with 认证信息*/
                .zipWith(relationMono)
                .doOnError(t -> logger.error("Zipping with FriendsRelation ", t))
                /*Message上设置发送人信息*/
                .map(tuple2 -> {
                    FriendsRelationDBEntity relationDBEntity = tuple2.getT2();
                    tuple2.getT1().setDeliver(relationDBEntity.getPersonID());//注意与relationMono的对应关系
                    tuple2.getT1().setDomainID(relationDBEntity.getDomainID());//注意与relationMono的对应关系
                    return tuple2.getT1();
                })
                /*存储到Mongodb，并生成ID*/
                //.doOnNext(t-> logger.error("Before save to mongodb,the messageDBEntity is:{}",JSON.toJSONString(t)))
                .flatMap(m -> messageStorageBroker.save(m))
                .doOnError(t -> logger.error("Error when save messageDBEntity in mongodb", t))
                /*存储到Redis*/
                //TODO Weather returning a messageDBEntity is necessary or not?
                .flatMap(m -> messageQueueBroker.pushMessage(m, true))
                .doOnError(t -> logger.error("Error when save messageDBEntity to redis,", t));

        //方案二，zip with auth info mono
        /*return Mono.just(messageDBEntity)
                .log(logger)
                *//*Token得到的Auth信息 zip with 认证信息*//*
                .zipWith(authInfoMono)
                .doOnError(t -> logger.error("Zipping with AuthInfo which get from remote error", t))
                *//*Message上设置发送人信息*//*
                .map(tuple2 -> {
                    AuthInfo authInfo = tuple2.getT2();
                    tuple2.getT1().setDeliver(authInfo.getUser().getId());
                    tuple2.getT1().setDomainID(authInfo.getDomain());
                    return tuple2.getT1();
                })
                *//*存储到Mongodb，并生成ID*//*
                //.doOnNext(t-> logger.error("Before save to mongodb,the messageDBEntity is:{}",JSON.toJSONString(t)))
                .flatMap(m -> messageStorageBroker.save(m))
                .doOnError(t -> logger.error("Error when save messageDBEntity in mongodb", t))
                *//*存储到Redis*//*
                //TODO Weather returning a messageDBEntity is necessary or not?
                .flatMap(m -> messageQueueBroker.pushMessage(m, true))
                .doOnError(t -> logger.error("Error when save messageDBEntity to redis,", t));*/
    }

    /**
     * 默认最多20条向前追溯
     *
     * @param token
     * @param theOther
     * @param latestMessageID
     * @param limit
     * @return
     */
    public Flux<ECommerceChartMessage> collectHistoryTraceBack(
            String token,
            String theOther,
            long latestMessageID,
            int limit) {
        return securityHandler.validateToken(token)
                .flatMapMany(c ->
                        messageStorageBroker.findSectionRelativeLimit(
                                latestMessageID, 0L, c.getDomain(), c.getUser().getId(), theOther, true, limit))
                .map(c -> (ECommerceChartMessage) c)
                .log(logger)
                .doOnError(t -> logger.error("Exception when nvoke findSectionRelativeLimit: ", t));
    }


    public Flux<ECommerceChartMessage> collectHistorySection(
            String token,
            String sender,
            long fromMessageID,
            long toMessageID,
            int limit) {
        return securityHandler.validateToken(token)
                .flatMapMany(c ->
                        messageStorageBroker.findSectionRelativeLimit(
                                fromMessageID, toMessageID, c.getDomain(), c.getUser().getId(), sender, false, limit))
                .map(c -> (ECommerceChartMessage) c)
                .log(logger)
                .doOnError(t -> logger.error("Exception when nvoke findSectionRelativeLimit: ", t));
    }


    private MessageDBEntity createMessageMaxIDForReceiver(String domain, String user) {
        MessageDBEntity messageDBEntity = new MessageDBEntity();
        messageDBEntity.setMessageID(Long.MAX_VALUE);
        messageDBEntity.setDomainID(domain);
        messageDBEntity.setReceiver(user);
        return messageDBEntity;
    }

    public Mono<Void> webSocketRealtimeHandler(WebSocketSession session, Mono<AuthInfo> authInfoMono, long latestMessageID) {

        //传入-1
        //


        final long finalLatestMessageID = latestMessageID <= 0 ? Long.MAX_VALUE : latestMessageID;
        ObjectMapper messageMapper = new ObjectMapper();

        /*authInfoMono.cache()
                .flatMapMany(authInfo ->
                            session.receive()
                                    .map(WebSocketMessage::getPayloadAsText)
                                    .map(content-> getAndValidateMessageFromText(messageMapper,content))
                                    .doOnError(throwable -> logger.error("(websocket)Error when convert content to MessageDBEntity",throwable))
                                    .map(message->{
                                        message.setDomainID(authInfo.getDomain());
                                        message.setDeliver(authInfo.getUser().getId());
                                        message.setDeliveryTime(new Date());
                                        message.setGenerateTime(message.getDeliveryTime());
                                        message.setMessageType(ECommerceChartMessage.MessageTypeEnum.PLAIN);
                                        message.setMessageDirection(ECommerceChartMessage.MessageDirectionEnum.UNKNOWN);
                                        return message;
                                    }))
                .flatMap(message -> messageStorageBroker.save(message))
                .doOnError(t -> logger.error("(websocket)Error when save message in mongodb", t))
                .flatMap(message -> messageQueueBroker.pushMessage(message))// put it receiver's queue
                .doOnError(t -> logger.error("(websocket)Error when save message to redis,", t))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                //.doOnSubscribe(c -> logger.info("--- .log(logger) 3"))
                .as(session::send)
                .doOnError(throwable -> logger.error("(websocket)Error when send message back to the delivery."));*/

        return authInfoMono.cache()
                //获取队列中最早一条消息，如果不存在，则返回一个 messageID = Long.MAX_VALUE 的Message Entity
                .flatMap(authInfo ->
                        messageQueueBroker.earliestMessage(authInfo.getDomain(), authInfo.getUser().getId())
                                .defaultIfEmpty(createMessageMaxIDForReceiver(authInfo.getDomain(), authInfo.getUser().getId())))
                //获取区间消息：从"队列最早一条" ---> "传入的MessageID"
                .flatMapMany(message ->
                        messageStorageBroker.findSectionRelativeLimit(
                                finalLatestMessageID, message.getMessageID(), message.getDomainID(), message.getReceiver(), false, Integer.MAX_VALUE))
                .concatWith(
                        authInfoMono.cache().flatMapMany(authInfo ->
                                messageQueueBroker.popMessage(authInfo.getDomain(), authInfo.getUser().getId()).repeat()))
                .doOnError(t -> logger.error("(websocket)Error when repeatedly get message from queue", t))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                .doOnError(t -> logger.error("(websocket)Error when transfer String to Text MessageDBEntity", t))
                .as(session::send);

    }

    private MessageDBEntity getAndValidateMessageFromText(ObjectMapper objectMapper, String content) {
        try {
            Assert.isTrue(!Strings.isNullOrEmpty(content), "The messageDBEntity must not be either null or empty  ");
            MessageDBEntity messageDBEntity = objectMapper.readValue(content, MessageDBEntity.class);
            Assert.isNull(messageDBEntity.getReceiver(), "Receiver must not be null");
            Assert.isTrue(!Strings.isNullOrEmpty(messageDBEntity.getContent()), "The content of the messageDBEntity must not be either null or empty");
            return messageDBEntity;
        } catch (Exception e) {
            logger.error("Can not parse content to A MessageDBEntity object,content is :{}", content);
            throw new RuntimeException("Illegal message", e);
        }
    }

}
