package uk.gov.hmcts.reform.idam.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

@Configuration
public class LdapConfig {

    @Autowired
    private ConfigProperties configProperties;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(configProperties.getLdap().getRoot());
        contextSource.setUserDn(configProperties.getLdap().getPrincipal());
        contextSource.setPassword(configProperties.getLdap().getPassword());
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}
