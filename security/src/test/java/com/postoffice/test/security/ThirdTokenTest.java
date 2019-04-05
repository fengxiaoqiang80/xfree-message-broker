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
        securityHandler.token("1bed312b-0833-40e4-82e3-7693e738876b")
                .doOnNext(System.out::println)
                .block(Duration.ofSeconds(5));
    }

}
