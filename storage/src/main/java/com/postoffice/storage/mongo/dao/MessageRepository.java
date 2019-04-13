package com.postoffice.storage.mongo.dao;

import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository
  extends ReactiveCrudRepository<MessageDBEntity, Long> {
  
    /*Flux<MessageDBEntity> findAllByValue(Long value);
    Mono<MessageDBEntity> findFirstByOwner(Mono<String> owner);*/

}