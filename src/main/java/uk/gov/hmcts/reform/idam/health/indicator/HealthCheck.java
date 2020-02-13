package uk.gov.hmcts.reform.idam.health.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeIndicator;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

@Component
public class HealthCheck implements HealthIndicator {

    private final List<HealthProbeIndicator> healthProbeList;

    public HealthCheck(List<HealthProbeIndicator> healthProbeList) {
        this.healthProbeList = healthProbeList;
    }

    @Override
    public Health health() {
        final Health.Builder builder;
        List<HealthProbeIndicator> patients = healthProbeList.stream().filter(
                indicator -> !indicator.isOkay()).collect(Collectors.toList());
        if (patients.isEmpty()) {
            builder = Health.up();
        } else {
            builder = Health.down();
            patients.stream().forEach(indicator -> {
                        String details = indicator.getDetails();
                        if (details != null) {
                            builder.withDetail(indicator.getClass().getName(), details);
                        }});
        }
        return builder
                .withDetail("v", defaultIfEmpty(getClass().getPackage().getImplementationVersion(), "dev"))
                .build();
    }
}
