package com.postoffice.test.storage.mongo;


import com.alibaba.fastjson.JSON;
import com.postoffice.storage.mongo.IDGenerator;
import com.postoffice.storage.mongo.IDSequence;
import com.postoffice.storage.mongo.Message;
import com.postoffice.storage.mongo.MessageRepository;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMongoConfiguration.class)
public class TestMongodb {
    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    MessageRepository messageRepository;

    @Test
    public void testTemplate() throws Exception{
        Assert.assertNotNull(template);
        Assert.assertNotNull(messageRepository);

        Message m = new Message();
        m.setContent("my first content");
        m.setGenerateTime(new Date());
        m.setDeliveryTime(m.getGenerateTime());

        messageRepository.save(m)
                .doOnError(System.err::println)
                //.doOnSuccess(System.t)
                .subscribe();
        messageRepository.findAll().subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(10);
    }


    @Autowired
    IDGenerator idGenerator;

    @Test
    @Ignore
    public void generateASequence() throws Exception{
        idGenerator.generateSEQ("message")
                .doOnError(System.err::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    @Ignore
    public void testSequence() throws Exception{

        Mono<IDSequence> sequenceMono = idGenerator.getNextSequenceMono("message");
        sequenceMono
                .map(JSON::toJSONString)
                .doOnNext(System.out::println)
                .repeat(5)
                .count()
                .block(Duration.ofSeconds(5));

    }
}
