package com.postoffice.test;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * create by jack 2018/6/2
 * WebSocket 客户端去调用 WebSokcet 协议
 */
public class WSClient {
    public static void main(final String[] args) throws Exception {

        //Flux<String>.interval();


        /**
         * ReactorNettyWebSocketClient 是 WebFlux 默认 Reactor Netty 库提供的 WebSocketClient 实现。
         execute 方法，与 ws://localhost:8080/echo 建立 WebSokcet 协议连接。
         execute 需要传入 WebSocketHandler 的对象，用来处理消息，这里的实现和前面的 EchoHandler 类似。
         通过 WebSocketSession 的 send 方法来发送字符串“你好”到服务器端，然后通过 receive 方法来等待服务器端的响应并输出。
         */

        final WebSocketClient client = new ReactorNettyWebSocketClient();
        /*client.execute(URI.create("ws://localhost:8080/ws/echo"),
                session ->
                        session.send(Flux.interval(Duration.ofSeconds(1)).map(l -> session.textMessage("你好")))
                                .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                                .doOnNext(System.out::println)
                                .repeat()
                                .then())
                .block(Duration.ofMillis(50000));*/


        //client.execute(URI.create("ws://localhost:9090/message/broker/collect/handshake/"+TestConfiguration.token+"/260"),
        client.execute(URI.create("wss://www.yogapro.cn/microshop/message/broker/collect/handshake/"+TestConfiguration.token+"/10"),
                session -> {
                    //session.send()
                    return session
                            .receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(System.out::println)
                            .repeat()
                            .then();
                }
        ).block(Duration.ofMillis(500000));


        /*WebClient.create("http://localhost:8080/temperature-stream").get().retrieve().bodyToFlux(ServerSentEvent.class)
                .subscribe(System.out::println);*/

        TimeUnit.SECONDS.sleep(300);

    }


   /* public class MySocketHandler implements WebSocketHandler{
        @Override
        public Mono<Void> handle(WebSocketSession session) {



            return null;
        }
    }*/


}
