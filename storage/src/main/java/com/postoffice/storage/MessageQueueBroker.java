package com.postoffice.storage;

import com.postoffice.storage.mongo.entity.MessageDBEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface MessageQueueBroker {
    /**
     * Push a messageDBEntity
     * @param messageDBEntity messageDBEntity
     * @param bothQueuesOfDeliverAndReceiver push messageDBEntity both queue of deliver and receiver
     * @return messageDBEntity count
     */
    Mono<MessageDBEntity> pushMessage(MessageDBEntity messageDBEntity, boolean bothQueuesOfDeliverAndReceiver);

    /**
     * Pop message frmo queue
     * @param domainID domainID
     * @param user user
     * @return message mono
     */
    Mono<MessageDBEntity> popMessage(String domainID, String user);

    /**
     * Pop message from queue with limit time
     * @param domainID domainID
     * @param user user
     * @param duration duration
     * @return message flux
     */
    Flux<MessageDBEntity> popMessage(String domainID, String user, Duration duration);
    /**
     * get earliest message in the queue
     * @param domainID domainID
     * @param user user
     * @return message mono
     */
    Mono<MessageDBEntity> earliestMessage(String domainID, String user);
}
