package com.amido.healthchecker.health.ds;

import com.amido.healthchecker.util.LdapProperties;
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
@Profile("ds")
public class CRSHealthIndicator implements HealthIndicator {

    private LdapProperties crsLdapProperties;
    private SecretHolder secretHolder;

    @Autowired
    public CRSHealthIndicator(LdapProperties ctsLdapProperties, SecretHolder secretHolder){
        this.crsLdapProperties = ctsLdapProperties;
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
        contextSource.setUrl(crsLdapProperties.getBaseUrl());
        contextSource.setBase(crsLdapProperties.getBase());
        contextSource.setUserDn(crsLdapProperties.getUserDN());
        contextSource.setPassword(secretHolder.getCrsLdapPassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }
}