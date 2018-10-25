package uk.gov.hmcts.reform.idam.healthchecker.health.ds;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.ldap.LdapHealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.healthchecker.util.DSProperties;
import uk.gov.hmcts.reform.idam.healthchecker.util.SecretHolder;

@Component
@Profile("ds-userstore")
public class DSUserStoreHealthIndicator implements HealthIndicator {

    private DSProperties dsUserStoreProperties;
    private SecretHolder secretHolder;

    @Autowired
    public DSUserStoreHealthIndicator(DSProperties dsUserStoreProperties, SecretHolder secretHolder){
        this.dsUserStoreProperties = dsUserStoreProperties;
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
        contextSource.setUrl(dsUserStoreProperties.getBaseUrl());
        contextSource.setBase(dsUserStoreProperties.getBase());
        contextSource.setUserDn(dsUserStoreProperties.getUserDN());
        contextSource.setPassword(secretHolder.getDSUserStorePassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }


}
