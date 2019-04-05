package com.postoffice.security;

import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Profile({"dev"})
@Service
public class DevTokenHandler implements SecurityHandler{

    private final String remoteURL;

    public DevTokenHandler(@Value("${security.auth.server}") String remoteURL){
        this.remoteURL = remoteURL;
    }

    public Mono<AuthInfo> token(String token){
        User user = new User();
        user.setId("myPersonalID_xx");
        AuthInfo authInfo = new AuthInfo();
        authInfo.setDomain("myshipid_xx");
        authInfo.setUser(user);
        authInfo.setLegalToken(true);
        authInfo.setMessage("token is "+token);
        return Mono.just(authInfo);
    }

    public Mono<SecurityProperties.User> user(String userID){
        return Mono.empty();
    }

}
