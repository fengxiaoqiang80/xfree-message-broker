package com.postoffice.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Date;

@Service
public class MessagePostOfficeService {

    private Logger logger = Loggers.getLogger(MessagePostOfficeService.class);

    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedis;

    @Autowired
    MessageStorageBroker messageStorageBroker;

    @Autowired
    MessageQueueBroker messageQueueBroker;

    @Autowired
    SecurityHandler securityHandler;


    public Mono<Message> delivery(
            @PathVariable String token,
            @PathVariable String receiver,
            @RequestBody String content) {
        Mono<AuthInfo> authInfoMono = securityHandler.token(token);
        Message message = new Message();
        message.setGenerateTime(new Date());
        message.setReceiver(receiver);
        message.setDeliveryTime(new Date());
        message.setMessageType(ECommerceChartMessage.MessageTypeEnum.UNKNOWN);
        message.setMessageDirection(ECommerceChartMessage.MessageDirectionEnum.UNKNOWN);
        message.setContent(content);
        return Mono.just(message)
                .log(logger)
                /*Token得到的Auth信息 zip with 认证信息*/
                .zipWith(authInfoMono)
                .doOnError(t -> logger.error("Zipping with AuthInfo which get from remote error", t))
                /*Message上设置发送人信息*/
                .map(tuple2 -> {
                    AuthInfo authInfo = tuple2.getT2();
                    tuple2.getT1().setDeliver(authInfo.getUser().getId());
                    tuple2.getT1().setDomainID(authInfo.getDomain());
                    return tuple2.getT1();
                })
                /*存储到Mongodb，并生成ID*/
                //.doOnNext(t-> logger.error("Before save to mongodb,the message is:{}",JSON.toJSONString(t)))
                .flatMap(m -> messageStorageBroker.save(m))
                .doOnError(t -> logger.error("Error when save message in mongodb", t))
                /*存储到Redis*/
                //TODO Weather returning a message is necessary or not?
                .flatMap(m -> messageQueueBroker.pushMessage(m,true))
                .doOnError(t -> logger.error("Error when save message to redis,", t));
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
            int limit
    ) {
        return securityHandler.token(token)
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
        return securityHandler.token(token)
                .flatMapMany(c ->
                        messageStorageBroker.findSectionRelativeLimit(
                                fromMessageID, toMessageID, c.getDomain(), c.getUser().getId(), sender, false, limit))
                .map(c -> (ECommerceChartMessage) c)
                .log(logger)
                .doOnError(t -> logger.error("Exception when nvoke findSectionRelativeLimit: ", t));
    }


    private Message createMessageMaxIDForReceiver(String domain, String user) {
        Message message = new Message();
        message.setMessageID(Integer.MAX_VALUE);
        message.setDomainID(domain);
        message.setReceiver(user);
        return message;
    }

    public Mono<Void> webSocketRealtimeHandler(WebSocketSession session, Mono<AuthInfo> authInfoMono, long latestMessageID) {

        final long finalLatestMessageID = latestMessageID <= 0 ? Long.MAX_VALUE : latestMessageID;
        ObjectMapper messageMapper = new ObjectMapper();

        /*authInfoMono.cache()
                .flatMapMany(authInfo ->
                            session.receive()
                                    .map(WebSocketMessage::getPayloadAsText)
                                    .map(content-> getAndValidateMessageFromText(messageMapper,content))
                                    .doOnError(throwable -> logger.error("(websocket)Error when convert content to Message",throwable))
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
                .flatMap(authInfo ->
                        messageQueueBroker.earliestMessage(authInfo.getDomain(), authInfo.getUser().getId())
                                .defaultIfEmpty(createMessageMaxIDForReceiver(authInfo.getDomain(), authInfo.getUser().getId())))
                .flatMapMany(message -> messageStorageBroker.findSectionRelativeLimit(
                        finalLatestMessageID, message.getMessageID(), message.getDomainID(), message.getReceiver(), false, Integer.MAX_VALUE))
                .concatWith(
                        authInfoMono.cache().flatMapMany(authInfo ->
                                messageQueueBroker.popMessage(authInfo.getDomain(), authInfo.getUser().getId()).repeat()))
                .doOnError(t -> logger.error("(websocket)Error when repeatedly get message from queue", t))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                .doOnError(t -> logger.error("(websocket)Error when transfer String to Text Message", t))
                .as(session::send);

    }

    private Message getAndValidateMessageFromText(ObjectMapper objectMapper,String content){
        try {
            Assert.isTrue(!Strings.isNullOrEmpty(content),"The message must not be either null or empty  ");
            Message message = objectMapper.readValue(content, Message.class);
            Assert.isNull(message.getReceiver(),"Receiver must not be null");
            Assert.isTrue(!Strings.isNullOrEmpty(message.getContent()),"The content of the message must not be either null or empty");
            return message;
        }catch (Exception e){
            logger.error("Can not make content A Message object,content is :{}",content);
            throw new RuntimeException("Illegal message",e);
        }
    }

}
