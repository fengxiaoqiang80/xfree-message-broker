package com.postoffice.storage;

import com.postoffice.storage.mongo.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface MessageQueueBroker {
    /**
     * Push a message
     * @param message message
     * @param bothQueuesOfDeliverAndReceiver push message both queue of deliver and receiver
     * @return message count
     */
    Mono<Message> pushMessage(Message message, boolean bothQueuesOfDeliverAndReceiver);

    /**
     * Pop message frmo queue
     * @param domainID domainID
     * @param user user
     * @return message mono
     */
    Mono<Message> popMessage(String domainID, String user);

    /**
     * Pop message from queue with limit time
     * @param domainID domainID
     * @param user user
     * @param duration duration
     * @return message flux
     */
    Flux<Message> popMessage(String domainID, String user, Duration duration);
    /**
     * get earliest message in the queue
     * @param domainID domainID
     * @param user user
     * @return message mono
     */
    Mono<Message> earliestMessage(String domainID, String user);
}
