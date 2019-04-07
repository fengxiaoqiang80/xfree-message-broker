package com.postoffice.controller;

import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.mongo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;

@RestController
@RequestMapping(MessagePostOffice.BASE_MAPPING)
public class MessagePostOffice {
    private Logger logger = Loggers.getLogger(MessagePostOffice.class);

    //public final static String WS_URL_PATTERN_BROKER = "/message/broker/collect/{token}/{latestMessageID}";
    public final static String BASE_MAPPING = "/message/broker";
    public final static String WS_URL_PATTERN_BROKER = BASE_MAPPING + "/collect/handshake/{token}/{latestMessageID}";
    private final UriTemplate uriTemplate = new UriTemplate(WS_URL_PATTERN_BROKER);

    @Autowired
    private MessagePostOfficeService postOfficeService;

    @Autowired
    SecurityHandler securityHandler;

    /**
     * Delivering message to a receiver
     *
     * @param token    Third part system token
     * @param receiver The receiver id
     * @param content  Content of message
     * @return Message ID of post office
     */
    @PostMapping("/send/{token}/{receiver}")
    public Mono<Message> delivery(
            @PathVariable String token,
            @PathVariable String receiver,
            @RequestBody String content
    ) {
        return postOfficeService.delivery(token, receiver, content);
    }

    /**
     * 向前追溯
     *
     * @param token
     * @param theOther
     * @param latestMessageID
     * @return
     */
    @GetMapping("/collect/history/trace/{token}/{theOther}/{latestMessageID}/{limit}")
    public Flux<ECommerceChartMessage> collectHistoryTraceBack(
            @PathVariable String token,
            @PathVariable String theOther,
            @PathVariable long latestMessageID,
            @PathVariable int limit
    ) {
        return postOfficeService.collectHistoryTraceBack(token, theOther, latestMessageID, limit);
    }

    @GetMapping("/collect/history/section/{token}/{theOther}/{fromMessageID}/{toMessageID}/{limit}")
    public Flux<ECommerceChartMessage> collectHistorySection(
            @PathVariable String token,
            @PathVariable String theOther,
            @PathVariable long fromMessageID,
            @PathVariable long toMessageID,
            @PathVariable int limit
    ) {
        return postOfficeService.collectHistorySection(token, theOther, fromMessageID, toMessageID, limit);
    }

    public WebSocketHandler webSocketHandler() {
        return session -> {
            Tuple2<String, String> tuple2 = getParameterFromURI(session.getHandshakeInfo().getUri().toString(), "token", "latestMessageID");
            logger.info("New client connect in, token:{} and latest message id:{}", tuple2.getT1(), tuple2.getT2());
            String token = tuple2.getT1();
            Mono<AuthInfo> authInfo = securityHandler.token(token);
            //TODO how to make following code reactive, it is block-pattern.
            //AuthInfo authInfo = authInfoMono.block(Duration.ofSeconds(10));
            /*if(!authInfo.isLegalToken()){
                return session.send(Mono.just(session.textMessage("Invalidate token")));
            }*/

            long latestMessageID = Long.valueOf(tuple2.getT2());


            return postOfficeService.webSocketRealtimeHandler(session, authInfo, latestMessageID);
        };
    }

    private Tuple2<String, String> getParameterFromURI(String uri, String key1, String key2) throws RuntimeException {
        Map<String, String> map = uriTemplate.match(uri);
        return Tuples.of(map.get(key1), map.get(key2));
    }

}
