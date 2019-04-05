package com.postoffice.security;

import com.alibaba.fastjson.JSON;
import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.User;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@Profile("default")
@Service
public class ThirdPartTokenHandler implements SecurityHandler {

    private Logger logger = Loggers.getLogger(ThirdPartTokenHandler.class);

    private final String serverBaseURL;
    private final String getUserByTokenURL;
    private final String validateTokenURL;

    public ThirdPartTokenHandler(
            @Value("${security.auth.serverBaseURL}") String serverBaseURL,
            @Value("${security.auth.validateTokenURL}") String validateTokenURL,
            @Value("${security.auth.getUserByTokenURL}") String getUserByTokenURL) {
        this.serverBaseURL = serverBaseURL;
        this.validateTokenURL = validateTokenURL;
        this.getUserByTokenURL = getUserByTokenURL;
    }

    public Mono<AuthInfo> token(String token) {
        //logger.error("Who call me...");
        return Mono.from(getUserInfo(token))
                .log(logger)
                .doOnError(t -> logger.error("Getting user information by token error", t))
                .map(thirdPartUser -> {
                    AuthInfo authInfo = new AuthInfo();
                    authInfo.setDomain(thirdPartUser.shopId);
                    User user = new User();
                    user.setId(thirdPartUser.personnelId);
                    authInfo.setUser(user);
                    return authInfo;
                });
    }

    /*public Mono<SecurityProperties.User> user(String userID) {
        return Mono.empty();
    }*/


    public Mono<ThirdPartUser> getUserInfo(String token) {
        return remoteGet(getUserByTokenURL, token)
                .bodyToMono(ThirdPartUserInfo.class)
                /*.flatMap(c->
                        c.isSuccess() ?
                                Mono.just(c):
                                Mono.error(new RuntimeException("Getting remote user unsuccessfully,the content is "+ JSON.toJSONString(c))))
                */
                .map(c->c.getData())
                .doOnError(t -> logger.error("Getting remote user information error", t));
    }


    public WebClient.ResponseSpec remoteGet(String url, String token) {

        return WebClient.create(url)
                .method(HttpMethod.GET)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "bearer " + token)
                .retrieve()
                .onStatus(
                        c -> !(c.is1xxInformational() || c.is2xxSuccessful()),//断言
                        d -> d.bodyToMono(String.class).flatMap(content ->
                                Mono.error(new RuntimeException("Response content is " + content))//包装异常
                        )
                );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ThirdPartUserInfo {
        private String bizCode;
        private String bizMessage;
        private boolean success;
        @EqualsAndHashCode.Include
        private ThirdPartUser data;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ThirdPartUser {
        private String identity;
        private String mobile;
        @EqualsAndHashCode.Include
        private String personnelId;
        private String shopId;
    }

}
