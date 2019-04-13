package com.postoffice.controller;

import com.postoffice.datamodel.AuthInfo;
import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.security.SecurityHandler;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
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
@RequestMapping(CtrlConst.BASE_MAPPING)
public class MessageCtrl {
    private Logger logger = Loggers.getLogger(MessageCtrl.class);

    public final static String WS_URL_PATTERN_BROKER = CtrlConst.BASE_MAPPING + "/collect/handshake/{token}/{latestMessageID}";
    private final UriTemplate uriTemplate = new UriTemplate(WS_URL_PATTERN_BROKER);

    @Autowired
    private MessageService postOfficeService;

    @Autowired
    SecurityHandler securityHandler;

    /**
     * Delivering message to a receiver
     *
     * @param token    Third part system token
     * @param receiver The receiver id
     * @param content  Content of message
     * @return MessageDBEntity ID of post office
     */
    @PostMapping("/send/{token}/{receiver}")
    public Mono<MessageDBEntity> delivery(
            @PathVariable String token,
            @PathVariable String receiver,
            @RequestBody String content
    ) {
        return postOfficeService.delivery(token, receiver, content);
    }

    /**
     * 向前追溯，查询 latestMessageID 之前的数据
     *
     * @param token Token
     * @param theOther  聊天对方用户ID
     * @param latestMessageID 如果传入-1，表示查询最新数据
     * @return
     */
    @GetMapping("/collect/history/trace/{token}/{theOther}/{latestMessageID}/{limit}")
    public Flux<ECommerceChartMessage> collectHistoryTraceBack(
            @PathVariable String token,
            @PathVariable String theOther,
            @PathVariable long latestMessageID,
            @PathVariable int limit
    ) {
        if(latestMessageID == -1){
            latestMessageID = Long.MAX_VALUE;
        }
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
        if(toMessageID == -1){
            toMessageID = Long.MAX_VALUE;
        }
        return postOfficeService.collectHistorySection(token, theOther, fromMessageID, toMessageID, limit);
    }

    /**
     * Registered into Websocket Mapper
     * @return
     */
    public WebSocketHandler webSocketHandler() {
        return session -> {
            Tuple2<String, String> tuple2 = getParameterFromURI(session.getHandshakeInfo().getUri().toString(), "token", "latestMessageID");
            logger.info("New client connect in, token:{} and latest message id:{}", tuple2.getT1(), tuple2.getT2());
            String token = tuple2.getT1();
            Mono<AuthInfo> authInfo = securityHandler.validateToken(token);
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
