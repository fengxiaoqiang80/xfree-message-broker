package com.postoffice.storage.redis;

import com.postoffice.storage.mongo.entity.MessageDBEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    //https://www.programcreek.com/java-api-examples/index.php?api=org.springframework.data.redis.core.ReactiveRedisTemplate
    @Bean
    public ReactiveRedisTemplate<String, MessageDBEntity> reactiveJsonPostRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<MessageDBEntity> valueSerializer = new Jackson2JsonRedisSerializer<>(MessageDBEntity.class);

        RedisSerializationContext<String, MessageDBEntity> serializationContext = RedisSerializationContext
                .<String, MessageDBEntity>newSerializationContext(new StringRedisSerializer())
                .key(keySerializer)
                .hashKey(keySerializer)
                .value(valueSerializer)
                .hashValue(valueSerializer)
                .build();


        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
