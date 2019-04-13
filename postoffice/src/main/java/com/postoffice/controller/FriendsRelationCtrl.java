package com.postoffice.controller;

import com.postoffice.datamodel.FriendsDetail;
import com.postoffice.datamodel.FriendsRelation;
import com.postoffice.security.SecurityHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping(CtrlConst.BASE_MAPPING)
public class FriendsRelationCtrl {
    @Autowired
    FriendsRelationService friendsRelationService;

    @Autowired
    SecurityHandler securityHandler;

    @GetMapping("/friends/list/{token}/{relationIDFrom}/{maximumDate}/{limit}")
    public Flux<FriendsRelation> findFrientsRelation(
            @PathVariable String token,
            @PathVariable long relationIDFrom,
            @PathVariable long maximumDate,
            @PathVariable int limit) {
        Optional<Long> optionalRelationIDFrom = Optional.ofNullable(relationIDFrom < 0 ? null : relationIDFrom);
        Optional<Date> optionalMaximumDate = Optional.ofNullable(maximumDate < 0 ? null : new Date(maximumDate));
        Optional<Integer> optionalLimit = Optional.ofNullable(limit < 1 ? null : limit);

        return securityHandler.validateToken(token)
                .cache()
                .flatMapMany(authInfo ->
                        friendsRelationService.findFrientsRelation(
                                authInfo.getDomain(),
                                authInfo.getUser().getId(),
                                optionalMaximumDate,
                                optionalRelationIDFrom,
                                optionalLimit));
    }

    @GetMapping("/friends/list/detail/{token}/{relationIDFrom}/{maximumDate}/{limit}")
    public Flux<FriendsDetail> findFrientsDetail(
            @PathVariable String token,
            @PathVariable long relationIDFrom,
            @PathVariable long maximumDate,
            @PathVariable int limit) {
        Optional<Long> optionalRelationIDFrom = relationIDFrom <= 0 ? Optional.empty() : Optional.of(relationIDFrom);
        Optional<Date> optionalMaximumDate = maximumDate <= 0 ? Optional.empty() : Optional.of(new Date(maximumDate));
        Optional<Integer> optionalLimit = limit < 1 ? Optional.empty() : Optional.of(limit);

        return securityHandler.validateToken(token)
                .cache()
                .flatMapMany(authInfo ->
                        friendsRelationService.findFriends(
                                token,
                                authInfo.getDomain(),
                                authInfo.getUser().getId(),
                                optionalMaximumDate,
                                optionalRelationIDFrom,
                                optionalLimit));
    }

}
