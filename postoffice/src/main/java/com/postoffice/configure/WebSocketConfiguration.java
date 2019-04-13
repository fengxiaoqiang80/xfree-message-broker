package com.postoffice.configure;

import com.postoffice.controller.MessageCtrl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Collections;

@Configuration
public class WebSocketConfiguration {

    @Bean
    public HandlerMapping handlerMapping(MessageCtrl messagePostOffice) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Collections.singletonMap(MessageCtrl.WS_URL_PATTERN_BROKER, messagePostOffice.webSocketHandler()));
        mapping.setOrder(1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }


    @Bean
    ReactiveRedisTemplate<String,String> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory){
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }

}
