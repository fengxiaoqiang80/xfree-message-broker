package com.postoffice.storage;

import com.postoffice.storage.mongo.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


public class MessageService {

    MessageQueueBroker messageQueueBroker;
    MessageStorageBroker messageStorageBroker;

    public MessageService(MessageQueueBroker messageQueueBroker, MessageStorageBroker messageStorageBroker) {
        this.messageQueueBroker = messageQueueBroker;
        this.messageStorageBroker = messageStorageBroker;
    }

    //public Flux<Message>


}
