package uk.gov.hmcts.reform.idam.health.probe.idm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.RestHealthProbe;

import java.util.Map;

@Component
@Profile("idm")
@Slf4j
public class IdmPingHealthProbe extends RestHealthProbe<Map<String, String>> {

    private static final String STATE = "state";
    private static final String IDM_ACTIVE = "ACTIVE_READY";

    private static String ANONYMOUS_USER = "anonymous";
    private static String ANONYMOUS_PASSWORD = "anonymous";

    private final IdmHealthProbeProperties idmHealthProbeProperties;
    private final IdmProvider idmProvider;

    private final String authorization;

    public IdmPingHealthProbe(IdmHealthProbeProperties idmHealthProbeProperties, IdmProvider idmProvider) {
        super(idmHealthProbeProperties.getPing().getFreshnessInterval());
        this.idmHealthProbeProperties = idmHealthProbeProperties;
        this.idmProvider = idmProvider;
        this.authorization = "Basic " + encode(ANONYMOUS_USER, ANONYMOUS_PASSWORD);
    }

    @Override
    protected Map<String, String> makeRestCall() {
        return idmProvider.ping(authorization);
    }

    @Override
    protected boolean validateContent(Map<String, String> content) {
        return IDM_ACTIVE.equals(MapUtils.getString(content, STATE));
    }

    @Override
    protected void handleException(Exception e) {
        log.error("IDM Ping: " + e.getMessage());
    }

    @Scheduled(fixedDelayString = "#{@idmHealthProbeProperties.ping.checkInterval}")
    @Override
    protected void refresh() {
        log.info("IDM Ping refresh");
        super.refresh();
    }
}
