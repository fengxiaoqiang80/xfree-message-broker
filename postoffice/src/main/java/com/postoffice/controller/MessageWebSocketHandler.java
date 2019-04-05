package com.postoffice.controller;

import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MessageWebSocketHandler implements WebSocketHandler {

    MessageQueueBroker messageQueueBroker;
    MessageStorageBroker messageStorageBroker;

    public MessageWebSocketHandler(MessageQueueBroker messageQueueBroker, MessageStorageBroker messageStorageBroker) {
        this.messageQueueBroker = messageQueueBroker;
        this.messageStorageBroker = messageStorageBroker;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

//        session.receive().


        return null;
    }


    public Flux<Message> getMessageFromParameterTo() {
        return null;
    }


}
