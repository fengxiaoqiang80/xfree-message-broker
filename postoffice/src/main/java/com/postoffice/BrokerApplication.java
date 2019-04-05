package com.postoffice;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class BrokerApplication {

    public static void main(String[] args) {

        WebApplicationType webApplicationType = WebApplicationType.REACTIVE;
        //Hooks.onOperatorDebug();
        new SpringApplicationBuilder()
                .sources(BrokerApplication.class)
                .registerShutdownHook(true)
                .web(webApplicationType)
                .run(args);
    }

}