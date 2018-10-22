package com.amido.healthchecker;

import com.amido.healthchecker.azure.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class HealthcheckerApplication {

    @Value("${am.secret.name.password}")
    private String amSecretNamePassword;

    @Value("${am.secret.name.smoke.test.user.username}")
    private String amSecretNameSmokeTestUserUsername;

    @Value("${am.secret.name.smoke.test.user.password}")
    private String amSecretNameSmokeTestUserPassword;

    @Autowired
    VaultService vaultService;

    @PostConstruct
    public void init() {
        vaultService.loadSecret("AM_PASSWORD", amSecretNamePassword);
        vaultService.loadSecret("SMOKE_TEST_USER_USERNAME", amSecretNameSmokeTestUserUsername);
        vaultService.loadSecret("SMOKE_TEST_USER_PASSWORD", amSecretNameSmokeTestUserPassword);
    }

    public static void main(String[] args) {
        SpringApplication.run(HealthcheckerApplication.class, args);
    }
}
