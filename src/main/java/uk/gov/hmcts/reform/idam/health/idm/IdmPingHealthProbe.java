package uk.gov.hmcts.reform.idam.health.idm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("idm")
@Slf4j
public class IdmPingHealthProbe implements HealthProbe {

    private static final String STATE = "state";
    private static final String IDM_ACTIVE = "ACTIVE_READY";

    private static String ANONYMOUS_USER = "anonymous";
    private static String ANONYMOUS_PASSWORD = "anonymous";

    private final IdmProvider idmProvider;
    private final String authorization;

    public IdmPingHealthProbe(IdmProvider idmProvider) {
        this.idmProvider = idmProvider;
        this.authorization = "Basic " + encode(ANONYMOUS_USER, ANONYMOUS_PASSWORD);
    }

    @Override
    public boolean probe() {
        try {
            Map<String, String> pingResponse = idmProvider.ping(authorization);
            return IDM_ACTIVE.equals(MapUtils.getString(pingResponse, STATE));
        } catch (Exception e) {
            log.error("IDM ping: " + e.getMessage());
        }
        return false;
    }

    private String encode(String identity, String secret) {
        return Base64.getEncoder().encodeToString((identity + ":" + secret).getBytes());
    }
}
