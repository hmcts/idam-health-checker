package com.amido.healthchecker;

import com.amido.healthchecker.azure.AzureVaultService;
import com.amido.healthchecker.azure.DummyVaultService;
import com.amido.healthchecker.azure.VaultService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class HealthCheckConfiguration {

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
