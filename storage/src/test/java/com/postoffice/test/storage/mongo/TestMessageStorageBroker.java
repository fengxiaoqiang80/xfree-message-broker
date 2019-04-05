package com.postoffice.test.storage.mongo;

import com.alibaba.fastjson.JSON;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;


@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMongoConfiguration.class)
public class TestMessageStorageBroker {

    @Autowired
    MessageStorageBroker messageStorageBroker;

    @Test
    public void saveOne() {

        Message message = new Message();
        message.setContent("test_m1");
        message.setDeliveryTime(new Date());
        message.setReceiver("fxq");
        message.setDeliver("feng");
        message.setGenerateTime(new Date());

        messageStorageBroker.save(message)
                .doOnNext(System.out::println)
                .doOnError(System.err::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    public void saveMulti() throws Exception {

        messageStorageBroker.save(
                Flux.range(1, 60)
                        .map(c -> {
                            Message message = new Message();
                            message.setContent("test_" + c);
                            message.setDeliveryTime(new Date());
                            message.setReceiver("fxq_" + c%4);
                            message.setDeliver("fxq_" + c%3);
                            message.setGenerateTime(new Date());
                            return message;
                        }))
                .blockLast(Duration.ofSeconds(5));
    }

    @Test
    public void findAll() {
        messageStorageBroker.findAll()
                .map(m -> JSON.toJSONString(m))
                .doOnEach(System.out::println)
                .blockLast(Duration.ofSeconds(10));
    }

    @Test
    public void findSectionByID() {
        messageStorageBroker.findByID(20L)
                .map(JSON::toJSONString)
                .defaultIfEmpty("No such a message")
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    public void findSectionRelativeLimit() {
        messageStorageBroker.findSectionRelativeLimit(150L,-1L, "167", "fxq_1", "fxq_2",true, 40)
                .map(JSON::toJSONString)
                .defaultIfEmpty("No such a message")
                .doOnEach(System.out::println)
                .blockLast(Duration.ofSeconds(5));
    }

}
