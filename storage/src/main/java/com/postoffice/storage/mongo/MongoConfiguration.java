package com.postoffice.storage.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = MongoConfiguration.class)
public class MongoConfiguration {

    /*public final static String DATABASE_NAME = "postoffice";
    public final static String BEAN_TEMPLATE = "MessageStorage";

    @Bean(BEAN_TEMPLATE)
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient) {
        return new ReactiveMongoTemplate(mongoClient,DATABASE_NAME);
    }*/



}
