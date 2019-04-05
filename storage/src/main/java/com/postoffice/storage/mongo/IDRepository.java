package com.postoffice.storage.mongo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDRepository
  extends ReactiveCrudRepository<IDSequence, String> {
  
    /*Flux<Message> findAllByValue(Long value);
    Mono<Message> findFirstByOwner(Mono<String> owner);*/

}