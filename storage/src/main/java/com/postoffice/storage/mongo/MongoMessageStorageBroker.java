package com.postoffice.storage.mongo;

import com.postoffice.storage.MessageStorageBroker;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;
import java.util.Optional;

@Service
public class MongoMessageStorageBroker implements MessageStorageBroker {

    private final static String ID_SEQ_NAME = "message";
    Logger logger = Loggers.getLogger(MongoMessageStorageBroker.class);
    private ReactiveMongoTemplate template;
    private MessageRepository messageRepository;
    private IDGenerator idGenerator;

    public MongoMessageStorageBroker(ReactiveMongoTemplate template, MessageRepository messageRepository, IDGenerator idGenerator) {
        this.template = template;
        this.messageRepository = messageRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Flux<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Mono<Message> findByID(Long messageID) {
        return messageRepository.findById(messageID);
    }

    @Override
    public Flux<Message> findSectionByIDLimit(Long messageIDFrom, Long messageIDTo, String domainID, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.empty(), Optional.empty(), previous, limit);
    }

    @Override
    public Flux<Message> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String userA, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.of(userA), Optional.empty(), false, limit);
    }

    @Override
    public Flux<Message> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String userA, String userB, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.of(userA), Optional.of(userB), previous, limit);
    }

    public Flux<Message> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, Optional<String> domainID, Optional<String> userA, Optional<String> userB, boolean previous, int limit) {
        Criteria condition = previous
                ? Criteria.where("messageID").gt(messageIDTo).lt(messageIDFrom)    //向前查找 messageIDFrom > x > messageIDTo
                : Criteria.where("messageID").gt(messageIDFrom).lt(messageIDTo);   //向后查找 messageIDFrom < x < messageIDTo

        if (domainID.isPresent()) {
            condition.and("domainID").is(domainID.get());
        }

        if (userA.isPresent()) {
            if (userB.isPresent()) {
                Criteria criteriaAB = new Criteria().andOperator(
                        Criteria.where("deliver").is(userA.get()),
                        Criteria.where("receiver").is(userB.get()));

                Criteria criteriaBA = new Criteria().andOperator(
                        Criteria.where("deliver").is(userB.get()),
                        Criteria.where("receiver").is(userA.get()));
                Criteria ABorBA = new Criteria().orOperator(criteriaAB, criteriaBA);
                condition.andOperator(ABorBA);
            } else {
                Criteria criteriaAA = new Criteria().orOperator(
                        Criteria.where("deliver").is(userA.get()),
                        Criteria.where("receiver").is(userA.get()));
                condition.andOperator(criteriaAA);
            }
        }

        Query query = new Query(condition).limit(limit);
        return template.find(query, Message.class);
    }

    @Override
    public Mono<Message> save(Mono<Message> messageMono) {
        Mono<Message> mono = messageMono
                .log(logger)
                .doOnError(t -> logger.error("Broker save message error,", t))
                .zipWith(idGenerator.getNextSequenceMono(ID_SEQ_NAME))
                .doOnNext(tuple2 -> tuple2.getT1().setMessageID(tuple2.getT2().getSequenceValue()))
                .map(tuple2 -> tuple2.getT1());
        //.doOnSubscribe(t-> logger.debug(""));
        return mono.flatMap(
                c -> messageRepository
                        .save(c)
                        .doOnError(t -> Loggers.getLogger(MessageRepository.class).error("Repository save message error,", t))
        );
    }

    @Override
    public Mono<Message> save(Message messageMono) {
        return save(Mono.just(messageMono));
    }

    @Override
    public Flux<Message> save(Flux<Message> messageFlux) {

        return messageFlux.zipWith(idGenerator.getNextSequenceFlux(ID_SEQ_NAME))
                .map(tuple2 -> {
                    tuple2.getT1().setMessageID(tuple2.getT2().getSequenceValue());
                    return tuple2.getT1();
                })
                .doOnError(t -> logger.error("Fetching next sequence error", t))
                .transform(messageRepository::saveAll);
    }

    @Override
    public Flux<Message> save(List<Message> messageFlux) {
        return save(Flux.fromIterable(messageFlux));
    }

    @Override
    public Mono<Void> readReceipt(Long messageID, String deliver, String Receiver) {
        return null;
    }

    @Override
    public Mono<Void> revoke(Long messageID) {
        return null;
    }
}
