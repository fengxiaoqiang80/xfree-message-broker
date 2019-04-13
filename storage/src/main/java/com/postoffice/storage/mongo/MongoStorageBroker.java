package com.postoffice.storage.mongo;

import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.dao.MessageRepository;
import com.postoffice.storage.mongo.entity.FriendsRelationDBEntity;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MongoStorageBroker implements MessageStorageBroker {
    Logger logger = Loggers.getLogger(MongoStorageBroker.class);
    private ReactiveMongoTemplate template;
    private MessageRepository messageRepository;
    private IDGenerator idGenerator;

    public MongoStorageBroker(ReactiveMongoTemplate template, MessageRepository messageRepository, IDGenerator idGenerator) {
        this.template = template;
        this.messageRepository = messageRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Mono<FriendsRelationDBEntity> findFriendsRelationByID(Long relationID) {
        return template.findById(relationID, FriendsRelationDBEntity.class);
    }

    @Override
    public Flux<FriendsRelationDBEntity> findFriendsList(String domainID, String user, Optional<Date> maximumDate , Optional<Long> relationIDFrom, Optional<Integer> limit) {

        Criteria where = Criteria
                .where("domainID").is(domainID)
                .and("personID").is(user);
        if (maximumDate.isPresent()){
            where.and("latestTalkTime").lte(maximumDate.get());
        }
        if (relationIDFrom.isPresent()) {
            where.and("relationID").ne(relationIDFrom.get());
        }
        Query query = new Query(where);
        if(limit.isPresent()){
            query.limit(limit.get());
        }
        query.with(Sort.by(Sort.Direction.DESC, "latestTalkTime"));
        return template.find(query, FriendsRelationDBEntity.class);
    }

    @Override
    public Mono<FriendsRelationDBEntity> updateOrSaveFriend(String domainID, String personID, String friendID) {
        Criteria where = Criteria
                .where("domainID").is(domainID)
                .and("personID").is(personID)
                .and("friendID").is(friendID);
        Query query = new Query(where);

        Update update = new Update()
                .currentDate("latestTalkTime");

        return template.findAndModify(query, update, FriendsRelationDBEntity.class)
                .switchIfEmpty(save(domainID, personID, friendID));

        /*return Mono.just(update).zipWith(idGenerator.getNextSequenceMono(FriendsRelationDBEntity.ID_FRIENDS_RELATION_SEQ_NAME))
                .map(tuple2->tuple2.getT1().setOnInsert("relationID", tuple2.getT2().getSequenceValue()))
                .flatMap(updateNew -> template.upsert(query,updateNew,FriendsRelationDBEntity.class))
                .doOnError(throwable -> logger.error("Error when updateOrSaveFriend",throwable));*/

        /*return idGenerator.getNextSequenceMono(FriendsRelationDBEntity.ID_FRIENDS_RELATION_SEQ_NAME)
                .flatMap(idSequence -> {
                    update.setOnInsert("relationID", idSequence.getSequenceValue());
                    return template
                            .upsert(query, update, FriendsRelationDBEntity.class); })
                .doOnError(throwable -> logger.error("Error when updateOrSaveFriend",throwable));*/
    }

    @Override
    public Flux<MessageDBEntity> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Mono<MessageDBEntity> findByID(Long messageID) {
        return messageRepository.findById(messageID);
    }

    @Override
    public Flux<MessageDBEntity> findSectionByIDLimit(Long messageIDFrom, Long messageIDTo, String domainID, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.empty(), Optional.empty(), previous, limit);
    }

    @Override
    public Mono<MessageDBEntity> findRelativeLatest(String domainID, String personID, String friendID) {
        Criteria criteriaAB = new Criteria().andOperator(
                Criteria.where("deliver").is(personID),
                Criteria.where("receiver").is(friendID));

        Criteria criteriaBA = new Criteria().andOperator(
                Criteria.where("deliver").is(friendID),
                Criteria.where("receiver").is(personID));
        Criteria ABorBA = new Criteria().orOperator(criteriaAB, criteriaBA);
        Query query = new Query(ABorBA);//.limit(1);
        query.with(Sort.by(Sort.Direction.DESC, "messageID"));
        return template.findOne(query, MessageDBEntity.class);
    }

    @Override
    public Flux<MessageDBEntity> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String personID, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.of(personID), Optional.empty(), false, limit);
    }

    @Override
    public Flux<MessageDBEntity> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String personID, String friendID, boolean previous, int limit) {
        return findSectionRelativeLimit(messageIDFrom, messageIDTo, Optional.of(domainID), Optional.of(personID), Optional.of(friendID), previous, limit);
    }

    public Flux<MessageDBEntity> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, Optional<String> domainID, Optional<String> personID, Optional<String> friendID, boolean previous, int limit) {
        Criteria condition = previous
                ? Criteria.where("messageID").gt(messageIDTo).lt(messageIDFrom)    //向前查找 messageIDFrom > x > messageIDTo
                : Criteria.where("messageID").gt(messageIDFrom).lt(messageIDTo);   //向后查找 messageIDFrom < x < messageIDTo

        if (domainID.isPresent()) {
            condition.and("domainID").is(domainID.get());
        }

        if (personID.isPresent()) {
            if (friendID.isPresent()) {
                Criteria criteriaAB = new Criteria().andOperator(
                        Criteria.where("deliver").is(personID.get()),
                        Criteria.where("receiver").is(friendID.get()));

                Criteria criteriaBA = new Criteria().andOperator(
                        Criteria.where("deliver").is(friendID.get()),
                        Criteria.where("receiver").is(personID.get()));
                Criteria ABorBA = new Criteria().orOperator(criteriaAB, criteriaBA);
                condition.andOperator(ABorBA);
            } else {
                Criteria criteriaAA = new Criteria().orOperator(
                        Criteria.where("deliver").is(personID.get()),
                        Criteria.where("receiver").is(personID.get()));
                condition.andOperator(criteriaAA);
            }
        }
        Query query = new Query(condition).limit(limit);
        if (previous) {
            query.with(Sort.by(Sort.Direction.DESC, "messageID"));
        }
        return template.find(query, MessageDBEntity.class);
    }

    @Override
    public Mono<MessageDBEntity> save(Mono<MessageDBEntity> messageMono) {
        Mono<MessageDBEntity> mono = messageMono
                .log(logger)
                .doOnError(t -> logger.error("Broker save message error,", t))
                .zipWith(idGenerator.getNextSequenceMono(MessageDBEntity.ID_MESSAGE_SEQ_NAME))
                .doOnNext(tuple2 -> tuple2.getT1().setMessageID(tuple2.getT2().getSequenceValue()))
                .map(tuple2 -> tuple2.getT1());
        return mono.flatMap(
                c -> messageRepository
                        .save(c)
                        .doOnError(t -> Loggers.getLogger(MessageRepository.class).error("Repository save message error,", t))
        );
    }

    @Override
    public Mono<MessageDBEntity> save(MessageDBEntity messageDBEntityMono) {
        return save(Mono.just(messageDBEntityMono));
    }

    @Override
    public Flux<MessageDBEntity> save(Flux<MessageDBEntity> messageFlux) {

        return messageFlux.zipWith(idGenerator.getNextSequenceFlux(MessageDBEntity.ID_MESSAGE_SEQ_NAME))
                .map(tuple2 -> {
                    tuple2.getT1().setMessageID(tuple2.getT2().getSequenceValue());
                    return tuple2.getT1();
                })
                .doOnError(t -> logger.error("Fetching next sequence error", t))
                .transform(messageRepository::saveAll);
    }

    @Override
    public Flux<MessageDBEntity> save(List<MessageDBEntity> messageDBEntityFluxes) {
        return save(Flux.fromIterable(messageDBEntityFluxes));
    }

    @Override
    public Mono<Void> readReceipt(Long messageID, String deliver, String Receiver) {
        return null;
    }

    @Override
    public Mono<Void> revoke(Long messageID) {
        return null;
    }

    private Mono<FriendsRelationDBEntity> save(String domainID, String personID, String friendID) {
        Date date = new Date();
        FriendsRelationDBEntity friendsRelationDBEntity = new FriendsRelationDBEntity();
        friendsRelationDBEntity.setDomainID(domainID);
        friendsRelationDBEntity.setPersonID(personID);
        friendsRelationDBEntity.setFriendID(friendID);
        friendsRelationDBEntity.setGenerateTime(date);
        friendsRelationDBEntity.setLatestTalkTime(date);

        return Mono.just(friendsRelationDBEntity)
                .zipWith(idGenerator.getNextSequenceMono(FriendsRelationDBEntity.ID_FRIENDS_RELATION_SEQ_NAME))
                .map(tuple2 -> {
                    tuple2.getT1().setRelationID(tuple2.getT2().getSequenceValue());
                    return tuple2.getT1(); })
                .flatMap(entityWithID -> template.insert(entityWithID))
                .doOnError(throwable -> logger.error("Error when updateOrSaveFriend", throwable));
    }

}
