package com.amido.healthchecker.health.ds;

import com.amido.healthchecker.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class DSHealthIndicatorTest {

    private DSTokenStoreHealthIndicator healthIndicator;

    private AMSecretHolder amSecretHolder = new AMSecretHolder("am-passwordName", "smoke-test-user-username", "smoke-test-user-passwordName");
    private DSTokenStoreSecretHolder dsTokenStoreSecretHolder = new DSTokenStoreSecretHolder("ds-token-store-passwordName");
    private DSUserStoreSecretHolder dsUserStoreSecretHolder = new DSUserStoreSecretHolder("ds-user-store-passwordName");

    @Before
    public void setup(){
        SecretHolder secretHolder = new SecretHolder(amSecretHolder, dsTokenStoreSecretHolder, dsUserStoreSecretHolder);
        secretHolder.setSecretsMap("ds-token-store-passwordName", "Pa55word11");
        DSProperties ldapProperties = new DSProperties("ldap://localhost:1389", "cn=Directory Manager", "Pa55word11", "");
        healthIndicator = new DSTokenStoreHealthIndicator(ldapProperties, secretHolder);
    }


    @Test
    public void shouldCheckLdapHealth(){

        //when
        Health resultHealth = healthIndicator.health();

        //then
        log.info("DS health :: {}",resultHealth.getStatus().getCode());
        if(!resultHealth.getStatus().getCode().equals("UP")){
            assertThat(resultHealth.getStatus().getCode(), equalTo("DOWN"));
        }

    }


}

