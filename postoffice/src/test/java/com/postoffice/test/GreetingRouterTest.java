package com.postoffice.test;

import com.postoffice.controller.MessagePostOfficeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@ActiveProfiles("default")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)

public class GreetingRouterTest {
    @Autowired
    MessagePostOfficeService postOfficeService;


    @Test
    public void testFind(){
        postOfficeService.collectHistoryTraceBack(TestConfiguration.token,"1234567890",149L, 100)
                .doOnEach(System.out::println)
                .count()
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(100));
    }


}