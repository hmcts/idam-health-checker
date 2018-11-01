package uk.gov.hmcts.reform.idam.health.probe;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealthCheck implements HealthIndicator {

    private final List<ScheduledHealthProbe> healthProbeList;

    public HealthCheck(List<ScheduledHealthProbe> healthProbeList) {
        this.healthProbeList = healthProbeList;
    }

    @Override
    public Health health() {
        if (healthProbeList.stream().allMatch(p -> p.isOkay())) {
            return Health.up().build();
        }
        return Health.down().build();
    }
}
