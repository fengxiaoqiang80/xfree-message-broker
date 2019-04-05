package com.postoffice.storage.mongo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository
  extends ReactiveCrudRepository<Message, Long> {
  
    /*Flux<Message> findAllByValue(Long value);
    Mono<Message> findFirstByOwner(Mono<String> owner);*/

}