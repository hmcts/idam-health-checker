package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {

    private final HealthProbe healthProbe;

    public HealthCheck(HealthProbe healthProbe) {
        this.healthProbe = healthProbe;
    }

    @Override
    public Health health() {
        if (healthProbe.isOkay()) {
            return Health.up().build();
        }
        return Health.down().build();
    }
}
