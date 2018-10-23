package com.amido.healthchecker;

import com.amido.healthchecker.azure.ClientSecretKeyVaultCredential;
import com.amido.healthchecker.health.am.AMFeignClient;
import com.amido.healthchecker.health.idm.IDMFeignClient;
import com.amido.healthchecker.util.*;
import com.microsoft.azure.keyvault.KeyVaultClient;
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

    @Value("${ds.token.store.url}")
    private String dsTokenStoreURL;

    @Value("${ds.token.store.userDN: ldap://localhost:1389}")
    private String dsTokenStoreUserDN;

    @Value("${ds.user.store.url}")
    private String dsUserStoreURL;

    @Value("${ds.user.store.userDN}")
    private String dsUserStoreUserDN;

    @Value("${vault.client.id}")
    private String vaultClientId;

    @Value("${vault.client.key}")
    private String vaultClientKey;

    @Value("${idm.uri}")
    private String idmUri;

    @Value("${am.secret.name.smoke.test.user.password}")
    private String smokeTestUserPassword;

    @Value("${am.secret.name.password}")
    private String amPasswordName;

    @Value("${ds.token.store.secret.name.password}")
    private String dsTokenStorePasswordName;

    @Value("${ds.user.store.secret.name.password}")
    private String dsUserStorePasswordName;

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

        return Feign.builder().encoder(new FormEncoder()).target(IDMFeignClient.class, idmUri);

    }

    @Bean(name="dsTokenStoreProperties")
    DSProperties dsTokenStoreProperties(){
        return new DSProperties(dsTokenStoreURL, dsTokenStoreUserDN, "", "");
    }

    @Bean(name="dsUserStoreProperties")
    DSProperties dsUserStoreProperties(){
        return new DSProperties(dsUserStoreURL, dsUserStoreUserDN, "", "");
    }

    @Bean
    KeyVaultClient keyVaultClient(){
        return new KeyVaultClient(new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey));
    }


    @Bean
    SecretHolder secretHolder(){
        return new SecretHolder(amSecretHolder(), dsTokenStoreSecretHolder(), dsUserStoreSecretHolder());
    }

    @Bean
    AMSecretHolder amSecretHolder(){
        return new AMSecretHolder(amPasswordName, smokeTestUserPassword);
    }

    @Bean
    public Request.Options timeoutOptions() {
        return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
    }

    @Bean
    DSTokenStoreSecretHolder dsTokenStoreSecretHolder(){
        return new DSTokenStoreSecretHolder(dsTokenStorePasswordName);
    }

    @Bean
    DSUserStoreSecretHolder dsUserStoreSecretHolder(){
        return new DSUserStoreSecretHolder(dsUserStorePasswordName);
    }


}

