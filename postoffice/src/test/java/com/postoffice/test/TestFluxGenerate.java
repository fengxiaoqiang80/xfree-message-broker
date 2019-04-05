package com.postoffice.test;

import com.postoffice.BrokerApplication;
import com.postoffice.storage.MessageQueueBroker;
import com.postoffice.storage.MessageStorageBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@ActiveProfiles("default")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)

public class TestFluxGenerate {


    @Autowired
    MessageQueueBroker messageQueueBroker;

    @Autowired
    MessageStorageBroker messageStorageBroker;

    @Test
    public void testStorageAll(){
        messageStorageBroker.findAll()
                .doOnSubscribe(System.err::print)
                .blockLast(Duration.ofSeconds(100));
    }

    @Test
    public void testStorageLimit(){
        messageStorageBroker.findSectionRelativeLimit(270L,280L,"167","2",false,1000)
                .doOnEach(System.err::println)
                .blockLast(Duration.ofSeconds(100));
    }
}
