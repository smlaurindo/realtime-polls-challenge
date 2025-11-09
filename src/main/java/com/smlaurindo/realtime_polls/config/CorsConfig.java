package com.smlaurindo.realtime_polls.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("#{'${app.cors.allowed.origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${app.cors.allowed.methods}'.split(',')}")
    private List<String> allowedMethods;

    private static final String ALLOW_ALL = "*";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods(allowedMethods.toArray(new String[0]))
                .allowedHeaders(ALLOW_ALL);
    }
}

