package uk.gov.hmcts.reform.idam.health.probe.idm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("idm")
@Slf4j
public class IdmPingHealthStatus implements HealthStatus {

    private static final String STATE = "state";
    private static final String IDM_ACTIVE = "ACTIVE_READY";
    private static String ANONYMOUS_USER = "anonymous";
    private static String ANONYMOUS_PASSWORD = "anonymous";

    private final IdmProvider idmProvider;
    private final String authorization;

    public IdmPingHealthStatus(IdmProvider idmProvider) {
        this.idmProvider = idmProvider;
        this.authorization = "Basic " + Base64.getEncoder().encodeToString((ANONYMOUS_USER + ":" + ANONYMOUS_PASSWORD).getBytes());
    }

    @Override
    public Status determineStatus() {
        try {
            Map<String, String> response = idmProvider.ping(authorization);
            if (IDM_ACTIVE.equals(MapUtils.getString(response, STATE))) {
                log.info("IDM Ping OK");
                return Status.UP;
            }
        } catch (Exception e) {
            log.error("IDM: " + e.getMessage());
        }
        return Status.DOWN;
    }
}
