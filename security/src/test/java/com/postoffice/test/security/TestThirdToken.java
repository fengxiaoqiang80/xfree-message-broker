package com.postoffice.test.security;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class TestThirdToken {
    long loginTO = 167;
    String token = "cb75d0ea-be98-425d-8d12-dec2ced7fe39";

    String appID = "wx9308044613fae7f8";
    String openID = "omDVs5RretjvsMJP8ENSLdcFFM5U";
    String baseURL = "https://www.yogapro.cn/microshop/api";
    //String baseURL = "http://127.0.0.1:8080";
    String userURL = baseURL + "/microprogram/getloginOnUser";
    String tokenURL = baseURL + "/oauth/token?grant_type=microprogram_code&client_id=client_1&client_secret=secret&scope=read&code=%s&appId=%s";
    String loginShop = baseURL + "/microprogram/chooseBrowsedShop?shopId=%d";


    @Test
    public void testToken(){
        Mono<String> token = getToken();
        token.doOnNext(c->System.out.println("-------->"+c))
                .block(Duration.ofSeconds(10));
    }


    @Test
    public void tokenAndLogin() {
//        Mono<String> token = getToken();
//        token.doOnNext(c->System.out.println("-------->"+c));
//        token.block(Duration.ofSeconds(5));
        loginShop(loginTO,Mono.just(token))
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(10));
    }


    @Test
    public void getUserInfo() {
        remoteCall(userURL, token)
                .bodyToMono(String.class)
                .map(JSON::toJSONString)
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(10));
    }

    private Mono<String> loginShop(long shopID, Mono<String> token) {
        String url = String.format(loginShop, shopID);
        return token.flatMap(c -> remoteCallPost(url, c).bodyToMono(String.class));
    }


    private Mono<String> getToken() {
        String url = String.format(tokenURL, openID, appID);
        return WebClient.create(url)
                .method(HttpMethod.GET)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> JSON.parseObject(body).getString("access_token"));
    }


    private WebClient.ResponseSpec remoteCall(String url, String token) {
        return WebClient.create(url)
                .method(HttpMethod.GET)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "bearer " + token)
                .retrieve();
    }

    private WebClient.ResponseSpec remoteCallPost(String url, String token) {
        return WebClient.create(url)
                .method(HttpMethod.POST)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "bearer " + token)
                .retrieve();
    }


}
