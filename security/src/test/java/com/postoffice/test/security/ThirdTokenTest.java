package com.postoffice.test.security;

import com.postoffice.security.SecurityHandler;
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
public class ThirdTokenTest {

    @Autowired
    SecurityHandler securityHandler;

    @Test
    public void testSecurityHandle(){
        securityHandler.validateToken(TestThirdToken.token)
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(5));
    }

    @Test
    public void testFindPersonInfo(){
        securityHandler.findPersonInfo(TestThirdToken.token,"2")
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(5));
    }

}
