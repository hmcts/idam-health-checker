package com.amido.healthchecker;


import com.amido.healthchecker.health.AMFeignClient;
import com.amido.healthchecker.health.AmHealthIndicator;
import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConfiguration {

    @Bean
    AMFeignClient amFeignClient(){
        return Feign.builder().target(AMFeignClient.class, "http://localhost:8080/openam");
    }

    @Bean
    AmHealthIndicator amHealthIndicator(){
        return new AmHealthIndicator(amFeignClient());
    }

}
