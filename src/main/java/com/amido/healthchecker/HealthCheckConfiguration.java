package com.amido.healthchecker;

import com.amido.healthchecker.health.AMFeignClient;
import com.amido.healthchecker.health.AmHealthIndicator;
import feign.Feign;
import feign.form.FormEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConfiguration {

    @Value("${am.uri}")
    private String amUri;

    @Bean
    AMFeignClient amFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).target(AMFeignClient.class, amUri);
    }

    @Bean
    AmHealthIndicator amHealthIndicator() {
        return new AmHealthIndicator(amFeignClient());
    }
}
