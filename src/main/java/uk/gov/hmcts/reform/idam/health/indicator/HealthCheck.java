package uk.gov.hmcts.reform.idam.health.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

import java.util.List;

@Component
public class HealthCheck implements HealthIndicator {

    private final List<ScheduledHealthProbe> healthProbeList;

    public HealthCheck(List<ScheduledHealthProbe> healthProbeList) {
        this.healthProbeList = healthProbeList;
    }

    @Override
    public Health health() {
        if (healthProbeList.stream().allMatch(ScheduledHealthProbe::isOkay)) {
            return Health.up().build();
        }
        return Health.down().build();
    }
}
