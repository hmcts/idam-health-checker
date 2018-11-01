package uk.gov.hmcts.reform.idam.health.probe.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("am")
@Slf4j
public class AmIsAliveHealthProbe implements HealthProbe {

    private static final String ALIVE = "ALIVE";

    private final AmProvider amProvider;

    public AmIsAliveHealthProbe(AmProvider amProvider) {
        this.amProvider = amProvider;
    }

    @Override
    public boolean probe() {
        String isAliveResponse = amProvider.isAlive();
        return StringUtils.contains(isAliveResponse, ALIVE);
    }
}
