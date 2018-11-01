package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import uk.gov.hmcts.reform.idam.health.probe.env.ConfigProperties;

@Configuration
@Profile({"userstore", "tokenstore"})
public class SpringLdapConfiguration {

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
