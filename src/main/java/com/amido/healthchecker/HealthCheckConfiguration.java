package com.amido.healthchecker;

import com.amido.healthchecker.health.am.AMFeignClient;
import com.amido.healthchecker.health.am.AmHealthIndicator;
import com.amido.healthchecker.health.idm.IDMFeignClient;
import feign.Feign;
import feign.form.FormEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amido.healthchecker.azure.AzureVaultService;
import com.amido.healthchecker.azure.DummyVaultService;
import com.amido.healthchecker.azure.VaultService;

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

    @Bean
    AmHealthIndicator amHealthIndicator() {
        return new AmHealthIndicator(amFeignClient());
    }

    @Bean(name="vaultService")
    @Profile("dev")
    VaultService dummyVaultService() {
        return new DummyVaultService();
    }

    @Bean(name="vaultService")
    @Profile("live")
    VaultService azureVaultService() {
        return new AzureVaultService();
    }
}
