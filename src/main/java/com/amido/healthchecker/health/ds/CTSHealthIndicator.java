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
public class CTSHealthIndicator implements HealthIndicator {

    private LdapProperties ctsLdapProperties;
    private SecretHolder secretHolder;

    @Autowired
    public CTSHealthIndicator(LdapProperties ctsLdapProperties, SecretHolder secretHolder){
        this.ctsLdapProperties = ctsLdapProperties;
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
        contextSource.setUrl(ctsLdapProperties.getBaseUrl());
        contextSource.setBase(ctsLdapProperties.getBase());
        contextSource.setUserDn(ctsLdapProperties.getUserDN());
        contextSource.setPassword(secretHolder.getCtsLdapPassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }


}
