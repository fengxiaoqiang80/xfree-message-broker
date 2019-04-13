package com.postoffice.controller;

import com.postoffice.datamodel.FriendsDetail;
import com.postoffice.datamodel.FriendsRelation;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.MessageStorageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.Optional;

@Service
public class FriendsRelationService {

    @Autowired
    private MessageStorageBroker messageStorageBroker;

    @Autowired
    private SecurityHandler securityHandler;

    public Flux<FriendsRelation> findFrientsRelation(String domainID, String user, Optional<Date> maximumDate, Optional<Long> relationIDFrom, Optional<Integer> limit) {

        return messageStorageBroker.findFriendsList(domainID, user, maximumDate, relationIDFrom, limit)
                .map(entity -> FriendsRelation.builder()
                        .relationID(entity.getRelationID())
                        .personID(entity.getPersonID())
                        .friendID(entity.getFriendID())
                        .domainID(entity.getDomainID())
                        .generateTime(entity.getGenerateTime())
                        .latestTalkTime(entity.getLatestTalkTime())
                        .build()
                );
    }

    public Flux<FriendsDetail> findFriends(String token, String domainID, String user, Optional<Date> maximumDate, Optional<Long> relationIDFrom, Optional<Integer> limit) {
        Flux<FriendsDetail> flux = messageStorageBroker.findFriendsList(domainID, user, maximumDate, relationIDFrom, limit)
                .flatMap(entity ->
                        securityHandler.findPersonInfo(token, entity.getFriendID())
                                .map(remoteUser -> FriendsDetail.builder()
                                        .relationID(entity.getRelationID())
                                        .friendID(entity.getFriendID())
                                        .generateTime(entity.getGenerateTime())
                                        .latestTalkTime(entity.getLatestTalkTime())
                                        .headImgUrl(remoteUser.getHeadImgUrl())
                                        .nickname(remoteUser.getNickname())
                                        .build())
                );
        return flux;
    }

}
