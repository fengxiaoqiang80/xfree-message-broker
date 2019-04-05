package com.postoffice.test.storage.mongo;

import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.storage.mongo.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMongoConfiguration.class)
public class TestRedisTemplate {

    @Autowired
    ReactiveRedisTemplate<String, Message> template;

    @Test
    public void testSerialize(){

        Message message = new Message();
        message.setDomainID("domainID");
        message.setMessageID(100L);
        message.setMessageDirection(ECommerceChartMessage.MessageDirectionEnum.CUSTOMER_TO_SHOPKEEPER);
        message.setMessageType(ECommerceChartMessage.MessageTypeEnum.PLAIN);

        template.opsForValue().set("test_key",message).block(Duration.ofSeconds(10));
        template.opsForValue().get("test_key").doOnNext(System.out::println).block(Duration.ofSeconds(10));

    }

}
