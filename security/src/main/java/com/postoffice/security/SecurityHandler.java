package com.postoffice.security;

import com.postoffice.datamodel.AuthInfo;
import reactor.core.publisher.Mono;

public interface SecurityHandler {
    Mono<AuthInfo> token(String token);
}
