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
    private final String getPersonURL;

    public ThirdPartTokenHandler(
            @Value("${security.auth.serverBaseURL}") String serverBaseURL,
            @Value("${security.auth.validateTokenURL}") String validateTokenURL,
            @Value("${security.auth.getUserByTokenURL}") String getUserByTokenURL,
            @Value("${security.auth.getPersonURL}") String getPersonURL) {
        this.serverBaseURL = serverBaseURL;
        this.validateTokenURL = validateTokenURL;
        this.getUserByTokenURL = getUserByTokenURL;
        this.getPersonURL = getPersonURL;
    }

    @Override
    public Mono<AuthInfo> validateToken(String token) {
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

    @Override
    public Mono<User> findPersonInfo(String token, String personID) {
        return remoteGet(getPersonURL + "/" + personID, token)
                .bodyToMono(ThirdPersonInfo.class)
                .doOnNext(person -> {
                    if (!person.isSuccess()) {
                        logger.warn(
                                "Getting person info from auth server unsuccessfully,personID:{},remote info:{}",
                                personID,
                                JSON.toJSONString(person));
                    }
                })
                .filter(person -> person.isSuccess())
                .map(person ->
                        User.builder()
                                .headImgUrl(person.data.headImgUrl)
                                .nickname(person.data.nickname)
                                .mobile(person.data.mobile)
                                .build())
                .doOnError(t -> logger.error("Getting remote person information error", t));
    }

    private Mono<ThirdPartUser> getUserInfo(String token) {
        return remoteGet(getUserByTokenURL, token)
                .bodyToMono(ThirdPartUserInfo.class)
                .map(c -> c.getData())
                .doOnError(t -> logger.error("Getting remote user information error", t));
    }


    private WebClient.ResponseSpec remoteGet(String url, String token) {

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
    public static class ThirdRespBase {
        private String bizCode;
        private String bizMessage;
        private boolean success;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    //@EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ThirdPartUserInfo extends ThirdRespBase {
        @EqualsAndHashCode.Include
        private ThirdPartUser data;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    //@EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ThirdPartUser {
        private String identity;
        private String mobile;
        @EqualsAndHashCode.Include
        private String personnelId;
        private String shopId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    //@EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ThirdPersonInfo extends ThirdRespBase {
        private ThirdPerson data;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class ThirdPerson {
        private long id;
        private String appId;
        private String openid;
        private String mobile;
        private String nickname;
        private String headImgUrl;
        private boolean deleteFlag;
    }


}
