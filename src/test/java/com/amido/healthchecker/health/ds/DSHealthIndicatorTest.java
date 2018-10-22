package com.amido.healthchecker.health.ds;

import com.amido.healthchecker.util.LdapProperties;
import com.amido.healthchecker.util.SecretHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class DSHealthIndicatorTest {

    private CTSHealthIndicator healthIndicator;

    @Before
    public void setup() {
        SecretHolder secretHolder = new SecretHolder();
        secretHolder.setSecretsMap("cts-ldap-password", "Pa55word11");
        LdapProperties ldapProperties = new LdapProperties("ldap://localhost:1389", "cn=Directory Manager", "Pa55word11", "");
        healthIndicator = new CTSHealthIndicator(ldapProperties, secretHolder);
    }

    @Test
    public void shouldCheckLdapHealth() {

        //when
        Health resultHealth = healthIndicator.health();

        //then
        log.info("DS health: {}",resultHealth.getStatus().getCode());
        if (!resultHealth.getStatus().getCode().equals("UP")) {
            assertThat(resultHealth.getStatus().getCode(), equalTo("DOWN"));
        }
    }
}
