package com.postoffice.test;

import com.postoffice.datamodel.AuthInfo;
import com.postoffice.security.SecurityHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.time.Duration;


@ActiveProfiles("default")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)

public class ThirdSecurityTest {

    @Autowired
    SecurityHandler securityHandler;

    @Test
    public void getUser(){
        Mono<AuthInfo> authInfoMono = securityHandler.validateToken("1bed312b-0833-40e4-82e3-7693e738876b");
        authInfoMono.doOnNext(System.out::println)
                .block(Duration.ofSeconds(10));
    }


}
