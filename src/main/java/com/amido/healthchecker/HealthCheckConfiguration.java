package com.amido.healthchecker;

import com.amido.healthchecker.health.am.AMFeignClient;
import com.amido.healthchecker.health.idm.IDMFeignClient;
import feign.Feign;
import feign.form.FormEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConfiguration {

    @Value("${am.uri}")
    private String amUri;

    @Value("${idm.uri}")
    private String idmUri;

    @Bean
    AMFeignClient amFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).target(AMFeignClient.class, amUri);
    }

    @Bean
    IDMFeignClient idmFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).target(IDMFeignClient.class, idmUri);
    }
}
