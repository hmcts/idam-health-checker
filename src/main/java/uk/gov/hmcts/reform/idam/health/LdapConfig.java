package uk.gov.hmcts.reform.idam.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

@Configuration
@Profile({"tokenstore","userstore"})
public class LdapConfig {

    @Autowired
    private ConfigProperties configProperties;

    @Bean
    @Profile("userstore")
    public LdapContextSource contextSourceUserstore() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(configProperties.getLdap().getRoot());
        contextSource.setUserDn(configProperties.getLdap().getPrincipal());
        contextSource.setPassword(configProperties.getLdap().getUserStorePassword());
        return contextSource;
    }

    @Bean
    @Profile("tokenstore")
    public LdapContextSource contextSourceTokenstore() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(configProperties.getLdap().getRoot());
        contextSource.setUserDn(configProperties.getLdap().getPrincipal());
        contextSource.setPassword(configProperties.getLdap().getTokenStorePassword());
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
