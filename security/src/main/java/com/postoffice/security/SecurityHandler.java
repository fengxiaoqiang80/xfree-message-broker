package com.postoffice.security;

import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.User;
import reactor.core.publisher.Mono;

public interface SecurityHandler {
    Mono<AuthInfo> validateToken(String token);
    Mono<User> findPersonInfo(String token, String personID);
}
