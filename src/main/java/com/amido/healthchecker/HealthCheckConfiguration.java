package com.amido.healthchecker;

import com.amido.healthchecker.health.am.AMFeignClient;
import com.amido.healthchecker.health.idm.IDMFeignClient;
import feign.Feign;
import feign.Request;
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

    @Value("${feign.connect.timeout.millis:5000}")
    private int connectTimeoutMillis;

    @Value("${feign.read.timeout.millis:60000}")
    private int readTimeoutMillis;

    @Bean
    AMFeignClient amFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).options(timeoutOptions()).target(AMFeignClient.class, amUri);
    }

    @Bean
    IDMFeignClient idmFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).options(timeoutOptions()).target(IDMFeignClient.class, idmUri);
    }

    @Bean
    public Request.Options timeoutOptions() {
        return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
    }
}
