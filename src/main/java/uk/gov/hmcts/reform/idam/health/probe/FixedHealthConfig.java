package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"optimist","pessimist"})
public class FixedHealthConfig {

    @Bean
    @Profile("optimist")
    public HealthProbeIndicator optimist() {
        return new FixedHealthProbeIndicator(true);
    }

    @Bean
    @Profile("pessimist")
    public HealthProbeIndicator pessimist() {
        return new FixedHealthProbeIndicator(false);
    }

}
