package com.postoffice.test;

import com.postoffice.controller.FriendsRelationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Optional;


@ActiveProfiles("default")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)

public class FriendsRelationTest {
    private MockMvc mockMvc;

    @Autowired
    FriendsRelationService friendsRelationService;

    @Test
    public void getFrientsRelation() {
        friendsRelationService.findFrientsRelation("167", "2", Optional.empty(), Optional.empty(), Optional.empty())
                .doOnEach(f -> System.out.println("type-->" + f.getType().toString() + "\n entity-->" + f.get()))
                .count()
                .doOnNext(f -> System.out.println("count:" + f))
                .block(Duration.ofSeconds(100));
    }

    @Test
    public void getFriends() {
        friendsRelationService.findFriends(TestConfiguration.token, "167", "2", Optional.empty(), Optional.empty(), Optional.empty())
                .doOnEach(f -> System.out.println("type-->" + f.getType().toString() + "\n entity-->" + f.get()))
                .count()
                .doOnNext(f -> System.out.println("count:" + f))
                .block(Duration.ofSeconds(100));
    }


}
