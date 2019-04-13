package com.postoffice.storage;


import com.postoffice.storage.mongo.entity.FriendsRelationDBEntity;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MessageStorageBroker {

    Mono<FriendsRelationDBEntity> findFriendsRelationByID(Long relationID);
    /**
     * 查询用户'myID'的聊天对象列表
     * @param domainID 店铺id
     * @param user 用户id（或理解为客服ID)
     * @return 用户列表
     */
    Flux<FriendsRelationDBEntity> findFriendsList(String domainID, String user, Optional<Date> maximumDate , Optional<Long> relationIDFrom, Optional<Integer> limit);
    /**
     * 增加聊天关系
     * @param domainID 店铺id
     * @param personID 用户A ID（或理解为客服ID）
     * @param friendID 用户B（或理解为客户）
     * @return 执行（更新/新增）的结果
     */
    Mono<FriendsRelationDBEntity> updateOrSaveFriend(String domainID, String personID, String friendID);

    Flux<MessageDBEntity> findAll();

    Mono<MessageDBEntity> findByID(Long messageID);
    Flux<MessageDBEntity> findSectionByIDLimit(Long messageIDFrom, Long messageIDTo, String domainID, boolean previous, int limit);

    Mono<MessageDBEntity> findRelativeLatest(String domainID,String personID,String friendID);
    Flux<MessageDBEntity> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String personID, boolean previous, int limit);
    Flux<MessageDBEntity> findSectionRelativeLimit(Long messageIDFrom, Long messageIDTo, String domainID, String personID, String friendID, boolean previous, int limit);

    Mono<MessageDBEntity> save(Mono<MessageDBEntity> messageMono);
    Mono<MessageDBEntity> save(MessageDBEntity messageDBEntityMono);
    Flux<MessageDBEntity> save(Flux<MessageDBEntity> messageFlux);
    Flux<MessageDBEntity> save(List<MessageDBEntity> messageDBEntityFluxes);
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
