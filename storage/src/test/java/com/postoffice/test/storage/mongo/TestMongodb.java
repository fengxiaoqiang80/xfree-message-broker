package com.postoffice.test.storage.mongo;


import com.alibaba.fastjson.JSON;
import com.postoffice.storage.mongo.IDGenerator;
import com.postoffice.storage.mongo.entity.IDSequenceDBEntity;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import com.postoffice.storage.mongo.dao.MessageRepository;
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

        MessageDBEntity m = new MessageDBEntity();
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
    public void generateASequence(){
        idGenerator.generateSEQ(MessageDBEntity.ID_MESSAGE_SEQ_NAME)
                .doOnError(System.err::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    public void testSequence() {
        Mono<IDSequenceDBEntity> sequenceMono = idGenerator.getNextSequenceMono(MessageDBEntity.ID_MESSAGE_SEQ_NAME);
        sequenceMono
                .map(JSON::toJSONString)
                .doOnNext(System.out::println)
                .repeat(5)
                .count()
                .block(Duration.ofSeconds(5));

    }
}
