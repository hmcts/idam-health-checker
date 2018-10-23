package com.amido.healthchecker;

import com.amido.healthchecker.azure.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class HealthcheckerApplication {

    public static final String AM_PASSWORD = "AM_PASSWORD";
    public static final String SMOKE_TEST_USER_PASSWORD = "SMOKE_TEST_USER_PASSWORD";

    @Value("${am.secret.name.password}")
    private String amSecretNamePassword;

    @Value("${am.secret.name.smoke.test.user.password}")
    private String amSecretNameSmokeTestUserPassword;

    @Autowired
    VaultService vaultService;

    @PostConstruct
    public void init() {
        vaultService.loadSecret(AM_PASSWORD, amSecretNamePassword);
        vaultService.loadSecret(SMOKE_TEST_USER_PASSWORD, amSecretNameSmokeTestUserPassword);
    }

    public static void main(String[] args) {
        SpringApplication.run(HealthcheckerApplication.class, args);
    }
}
