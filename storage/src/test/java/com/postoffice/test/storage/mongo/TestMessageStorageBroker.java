package com.postoffice.test.storage.mongo;

import com.alibaba.fastjson.JSON;
import com.postoffice.storage.MessageStorageBroker;
import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;


@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMongoConfiguration.class)
public class TestMessageStorageBroker {

    @Autowired
    MessageStorageBroker messageStorageBroker;

    @Test
    public void saveOne() {

        MessageDBEntity messageDBEntity = new MessageDBEntity();
        messageDBEntity.setContent("test_m1");
        messageDBEntity.setDeliveryTime(new Date());
        messageDBEntity.setReceiver("fxq");
        messageDBEntity.setDeliver("feng");
        messageDBEntity.setGenerateTime(new Date());

        messageStorageBroker.save(messageDBEntity)
                .doOnNext(System.out::println)
                .doOnError(System.err::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    public void saveMulti() throws Exception {

        messageStorageBroker.save(
                Flux.range(1, 60)
                        .map(c -> {
                            MessageDBEntity messageDBEntity = new MessageDBEntity();
                            messageDBEntity.setContent("test_" + c);
                            messageDBEntity.setDeliveryTime(new Date());
                            messageDBEntity.setReceiver("fxq_" + c%4);
                            messageDBEntity.setDeliver("fxq_" + c%3);
                            messageDBEntity.setGenerateTime(new Date());
                            return messageDBEntity;
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


    @Test
    public void upsertFriends(){

        messageStorageBroker.updateOrSaveFriend("167","2","1")
                .doOnNext(updateResult -> System.out.println("Friends Relation:"+JSON.toJSONString(updateResult)))
                .doOnError(throwable -> System.err.println(throwable))
                .block(Duration.ofSeconds(10));

        messageStorageBroker.updateOrSaveFriend("167","2","3")
                .doOnNext(updateResult -> System.out.println("Friends Relation:"+JSON.toJSONString(updateResult)))
                .doOnError(throwable -> System.err.println(throwable))
                .block(Duration.ofSeconds(10));

        messageStorageBroker.updateOrSaveFriend("167","2","4")
                .doOnNext(updateResult -> System.out.println("Friends Relation:"+JSON.toJSONString(updateResult)))
                .doOnError(throwable -> System.err.println(throwable))
                .block(Duration.ofSeconds(10));

        messageStorageBroker.updateOrSaveFriend("167","2","5")
                .doOnNext(updateResult -> System.out.println("Friends Relation:"+JSON.toJSONString(updateResult)))
                .doOnError(throwable -> System.err.println(throwable))
                .block(Duration.ofSeconds(10));
    }


    @Test
    public void findFriendsRelation(){
        messageStorageBroker.findFriendsList("167","2", Optional.of(new Date(1555054316548L)),Optional.of(10L),Optional.of(1))
                .doOnEach(fr -> System.out.println("Friends Relation:"+JSON.toJSONString(fr.get())))
                .blockLast(Duration.ofSeconds(10));
    }

}
