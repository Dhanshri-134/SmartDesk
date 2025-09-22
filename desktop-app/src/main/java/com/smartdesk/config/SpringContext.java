package com.smartdesk.config;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.smartdesk.SmartDeskApp;

public class SpringContext {

    private static ConfigurableApplicationContext context;

    public static ConfigurableApplicationContext start() {
        if (context == null) {
            context = new SpringApplicationBuilder(SmartDeskApp.class)
                    .web(WebApplicationType.NONE) // desktop app; no Tomcat
                    .run();
        }
        return context;
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}
