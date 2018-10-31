package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConditionalOnProperty("ldap.root")
public class SpringLdapConfiguration {

    private final Environment environment;

    public SpringLdapConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();

        contextSource.setUrl(environment.getRequiredProperty("ldap.root"));
        contextSource.setBase(environment.getRequiredProperty("ldap.partitionSuffix"));
        contextSource.setUserDn(environment.getRequiredProperty("ldap.principal"));
        contextSource.setPassword(environment.getRequiredProperty("ldap.password"));

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}
