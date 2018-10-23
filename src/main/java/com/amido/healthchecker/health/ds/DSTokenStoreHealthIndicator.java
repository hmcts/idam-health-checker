package com.amido.healthchecker.health.ds;

import com.amido.healthchecker.util.DSProperties;
import com.amido.healthchecker.util.SecretHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.ldap.LdapHealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

@Component
@Profile("ds-tokenstore")
public class DSTokenStoreHealthIndicator implements HealthIndicator {

    private DSProperties dsTokenStoreProperties;
    private SecretHolder secretHolder;

    @Autowired
    public DSTokenStoreHealthIndicator(DSProperties dsTokenStoreProperties, SecretHolder secretHolder){
        this.dsTokenStoreProperties = dsTokenStoreProperties;
        this.secretHolder = secretHolder;
    }

    @Override
    public Health health() {
        return getLdapHealthIndicator().health();
    }


    private LdapHealthIndicator getLdapHealthIndicator(){

        LdapTemplate ldapTemplate = new LdapTemplate(getLdapContextSource());
        return new LdapHealthIndicator(ldapTemplate);
    }

    private LdapContextSource getLdapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(dsTokenStoreProperties.getBaseUrl());
        contextSource.setBase(dsTokenStoreProperties.getBase());
        contextSource.setUserDn(dsTokenStoreProperties.getUserDN());
        contextSource.setPassword(secretHolder.getDSTokenStorePassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }


}
