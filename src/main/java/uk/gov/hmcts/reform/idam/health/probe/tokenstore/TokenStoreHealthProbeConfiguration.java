package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

@Configuration
@Profile("tokenstore")
public class TokenStoreHealthProbeConfiguration {

    @Autowired
    private TokenStoreHealthProbeProperties tokenStoreHealthProbeProperties;

    @Bean
    public ScheduledHealthProbe tokenStoreSearchHealthProbe(
            TokenStoreSearchHealthProbe tokenStoreSearchHealthProbe) {
        return new ScheduledHealthProbe(
                tokenStoreSearchHealthProbe,
                tokenStoreHealthProbeProperties.getSearch().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getSearch().getCheckInterval());
    }
}
