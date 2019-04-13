package com.postoffice.storage.mongo.dao;

import com.postoffice.storage.mongo.entity.IDSequenceDBEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDRepository
  extends ReactiveCrudRepository<IDSequenceDBEntity, String> {
  
    /*Flux<MessageDBEntity> findAllByValue(Long value);
    Mono<MessageDBEntity> findFirstByOwner(Mono<String> owner);*/

}