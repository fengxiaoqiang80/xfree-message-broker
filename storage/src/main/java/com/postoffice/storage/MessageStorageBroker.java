package com.postoffice.storage;


import com.postoffice.storage.mongo.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MessageStorageBroker {

    Flux<Message> findAll();

    Mono<Message> findByID(Long messageID);
    Flux<Message> findSectionByIDLimit(Long messageIDFrom, Long messageIDTo, String domainID, boolean previous, int limit);
    Flux<Message> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String userA,boolean previous, int limit);
    Flux<Message> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String userA, String userB,boolean previous, int limit);

    Mono<Message> save(Mono<Message> messageMono);
    Mono<Message> save(Message messageMono);
    Flux<Message> save(Flux<Message> messageFlux);
    Flux<Message> save(List<Message> messageFlux);
    /**
     * 读回执
     * @param messageID
     * @param deliver
     * @param Receiver
     * @return
     */
    Mono<Void> readReceipt(Long messageID,String deliver,String Receiver);

    /**
     * 撤销
     * @param messageID
     * @return
     */
    Mono<Void> revoke(Long messageID);

}
