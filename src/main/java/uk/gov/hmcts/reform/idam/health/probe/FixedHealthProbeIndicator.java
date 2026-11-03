package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public class FixedHealthProbeIndicator implements HealthProbeIndicator, HealthIndicator {

    private final boolean fixedValue;

    public FixedHealthProbeIndicator(boolean fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public boolean isOkay() {
        return fixedValue;
    }

    @Override
    public Health health() {
        if (fixedValue) {
            return Health.up().build();
        } else {
            return Health.down().build();
        }
    }
}
