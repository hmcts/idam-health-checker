package com.amido.healthchecker;

import com.amido.healthchecker.azure.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class HealthcheckerApplication {

    @Autowired
    VaultService vaultService;

    @PostConstruct
    public void init() {
        vaultService.loadAllSecrets();
    }

    public static void main(String[] args) {
        SpringApplication.run(HealthcheckerApplication.class, args);
    }
}
