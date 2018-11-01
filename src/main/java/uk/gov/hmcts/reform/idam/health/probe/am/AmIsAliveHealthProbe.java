package uk.gov.hmcts.reform.idam.health.probe.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.RestHealthProbe;

@Component
@Profile("am")
@Slf4j
public class AmIsAliveHealthProbe extends RestHealthProbe<String> {

    private static final String ALIVE = "ALIVE";

    private final AmHealthProbeProperties amHealthProbeProperties;
    private final AmProvider amProvider;

    public AmIsAliveHealthProbe(AmHealthProbeProperties amHealthProbeProperties, AmProvider amProvider) {
        super(amHealthProbeProperties.getIsAlive().getFreshnessInterval());
        this.amHealthProbeProperties = amHealthProbeProperties;
        this.amProvider = amProvider;
    }

    @Override
    protected String makeRestCall() {
        return amProvider.isAlive();
    }

    @Override
    protected boolean validateContent(String content) {
        return StringUtils.contains(content, ALIVE);
    }

    @Override
    protected void handleException(Exception e) {
        log.error("AM Is Alive: " + e.getMessage());
    }

    @Scheduled(fixedDelayString = "${am.healthprobe.isalive.check-interval}")
    @Override
    protected void refresh() {
        log.info("Refreshing AM IsAlive");
        super.refresh();
    }
}
