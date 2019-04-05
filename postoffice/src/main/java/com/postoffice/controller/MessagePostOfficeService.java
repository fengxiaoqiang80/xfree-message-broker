package com.postoffice.controller;

import com.alibaba.fastjson.JSON;
import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

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


    public Mono<Long> delivery(
            @PathVariable String token,
            @PathVariable String receiver,
            @RequestBody String content) {
        Mono<AuthInfo> authInfoMono = securityHandler.token(token);
        Message message = new Message();
        message.setGenerateTime(new Date());
        message.setReceiver(receiver);
        message.setDeliveryTime(new Date());
        message.setMessageDirection(ECommerceChartMessage.MessageDirectionEnum.CUSTOMER_TO_SHOPKEEPER);
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
                .doOnError(t -> logger.error("Saving to mongodb error", t))
                /*存储到Redis*/
                //.doOnNext(t-> logger.error("Before save to redis,the message is:{}",JSON.toJSONString(t)))
                .flatMap(m -> messageQueueBroker.pushMessage(m))
                .doOnError(t -> logger.error("Saving to redis error,", t));

    }

    /**
     * 默认最多20条向前追溯
     *
     * @param token
     * @param sender
     * @param latestMessageID
     * @param limit
     * @return
     */
    public Flux<ECommerceChartMessage> collectHistoryTraceBack(
            String token,
            String sender,
            long latestMessageID,
            int limit
    ) {
        return securityHandler.token(token)
                .flatMapMany(c ->
                        messageStorageBroker.findSectionRelativeLimit(
                                latestMessageID, 0L, c.getDomain(), c.getUser().getId(), sender, true, limit))
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


    public void webSocketHistoryHandler(WebSocketSession session, Mono<AuthInfo> authInfoMono, long latestMessageID){

    }


    private Message createMessageMaxIDForReceiver(String domain,String user){
        Message message = new Message();
        message.setMessageID(Integer.MAX_VALUE);
        message.setDomainID(domain);
        message.setReceiver(user);
        return message;
    }

    public Mono<Void> webSocketRealtimeHandler(WebSocketSession session, Mono<AuthInfo> authInfoMono, long latestMessageID) {

        final long finalLatestMessageID = latestMessageID <= 0?Long.MAX_VALUE:latestMessageID;

        /*session.receive()
                .doOnEach()*/


        return authInfoMono.cache()
                .flatMap(authInfo ->
                        messageQueueBroker.earliestMessage(authInfo.getDomain(),authInfo.getUser().getId())
                                .defaultIfEmpty(createMessageMaxIDForReceiver(authInfo.getDomain(),authInfo.getUser().getId())))
                .flatMapMany(message -> messageStorageBroker.findSectionRelativeLimit(
                        finalLatestMessageID,message.getMessageID(),message.getDomainID(),message.getReceiver(),false,Integer.MAX_VALUE))
                .concatWith(
                        authInfoMono.cache().flatMapMany(authInfo ->
                                messageQueueBroker.popMessage(authInfo.getDomain(), authInfo.getUser().getId()).repeat())
                ).doOnSubscribe(c -> logger.info("--- .log(logger) 1"))
                //.repeat()
                //.doOnSubscribe(c -> logger.info("--- .log(logger) 2"))
                .doOnError(t -> logger.error("Error when repeatedly get message from queue", t))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                .doOnError(t -> logger.error("Error when transfer String to Text Message", t))
                //.doOnSubscribe(c -> logger.info("--- .log(logger) 3"))
                .as(session::send);


        /*return authInfoMono.cache()
                .flatMapMany(authInfo ->
                        messageQueueBroker.popMessage(authInfo.getDomain(), authInfo.getUser().getId()).repeat())
                .doOnSubscribe(c -> logger.info("--- .log(logger) 1"))
                //.repeat()
                //.doOnSubscribe(c -> logger.info("--- .log(logger) 2"))
                .doOnError(t -> logger.error("Error when repeatedly get message from queue", t))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                .doOnError(t -> logger.error("Error when transfer String to Text Message", t))
                //.doOnSubscribe(c -> logger.info("--- .log(logger) 3"))
                .as(session::send);*/

        //receive 类型：命令和数据。
        // 命令：向前找数据、向后找数据
        // 数据：投递的消息

        /*return reactiveRedis.opsForList()
                .leftPop("MSG_PO_D:167U:1234567890")
                .repeat()
                //.onError
                .index()
                .map(data -> new ECommerceChartMessage(
                        data.getT1(),//messageID
                        "deliver",//deliver
                        "receiver",//receiver
                        "domainID",//domainID
                        ECommerceChartMessage.MessageDirectionEnum.CUSTOMER_TO_SHOPKEEPER,//MessageDirectionEnum
                        ECommerceChartMessage.MessageTypeEnum.PLAIN,//MessageTypeEnum
                        String.format("%S", data.getT2()),//content
                        String.valueOf(data.getT1() % 5),//deliverInfos
                        "我是店主刘大海",//receiverInfos
                        new Date(),//generateTime
                        new Date(),//deliveryTime
                        new Date(),//readReceiptTime
                        new Date()//revokeTime
                ))
                .map(JSON::toJSONString)
                .map(session::textMessage)
                .as(session::send);*/

    }

}
