package com.amido.healthchecker;

import com.amido.healthchecker.azure.ClientSecretKeyVaultCredential;
import com.amido.healthchecker.health.am.AMFeignClient;
import com.amido.healthchecker.health.idm.IDMFeignClient;
import com.amido.healthchecker.util.LdapProperties;
import com.amido.healthchecker.util.SecretHolder;
import com.microsoft.azure.keyvault.KeyVaultClient;
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

    @Value("${CTS.ldap.url}")
    private String ctsLdapURL;

    @Value("${CTS.ldap.userDN: ldap://localhost:1389}")
    private String ctsLdapUserDN;

    @Value("${CRS.ldap.url}")
    private String crsLdapURL;

    @Value("${CRS.ldap.userDN}")
    private String crsLdapUserDN;

    @Value("${vault.client.id}")
    private String vaultClientId;

    @Value("${vault.client.key}")
    private String vaultClientKey;

    @Bean
    AMFeignClient amFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).target(AMFeignClient.class, amUri);
    }

    @Bean
    IDMFeignClient idmFeignClient() {
        return Feign.builder().encoder(new FormEncoder()).target(IDMFeignClient.class, idmUri);
    }

    @Bean(name="ctsLdapProperties")
    LdapProperties ctsLdapProperties() {
        return new LdapProperties(ctsLdapURL, ctsLdapUserDN, "", "");
    }

    @Bean(name="crsLdapProperties")
    LdapProperties crsLdapProperties() {
        return new LdapProperties(crsLdapURL, crsLdapUserDN, "", "");
    }

    @Bean
    KeyVaultClient keyVaultClient() {
        return new KeyVaultClient(new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey));
    }

    @Bean
    SecretHolder secretHolder() {
        return new SecretHolder();
    }
}
