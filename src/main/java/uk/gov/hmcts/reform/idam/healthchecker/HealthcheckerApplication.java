package uk.gov.hmcts.reform.idam.healthchecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.idam.healthchecker.azure.VaultService;

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
