package com.postoffice.test;


import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/*@Profile("dev")
@RunWith(SpringRunner.class)*/
public class MessageStorageBrokerTest {

    private int serverPort = 9090;

    @Test
    public void delivery_POST() {

        //WebClient client = WebClient.create("http://127.0.0.1:" + serverPort);
        WebClient client = WebClient.create("https://www.yogapro.cn/microshop");


        Mono.just(new AtomicInteger(100))
                .map(c -> c.getAndAdd(1) + "你好，多少?")
                .transform(c ->
                        client.post()
                                .uri("/message/broker/send/"+TestConfiguration.token+"/1")///send/{token}/{receiver}
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .body(c, String.class)
                                .retrieve()
                                .onStatus(
                                        status -> !status.is2xxSuccessful(),
                                        able -> Mono.error(new RuntimeException("Remote exception,response body is " + able.bodyToMono(String.class))))
                                .bodyToMono(String.class)
                                .doOnError(System.out::println)
                                .doOnNext(System.out::println))
                .repeat(10)
                .blockLast(Duration.ofSeconds(20));
    }

    @Test
    public void find() {
        WebClient.create("http://127.0.0.1:" + serverPort)
                .get()
                .uri("/message/broker/collect/history/trace/"+TestConfiguration.token+"/1234567890/1000/10")
                //.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        able -> Mono.error(new RuntimeException("Remote exception,response body is " + able.bodyToMono(String.class))))
                .bodyToFlux(String.class)
                .doOnEach(System.out::println)
                .blockLast(Duration.ofSeconds(100));
    }
}
