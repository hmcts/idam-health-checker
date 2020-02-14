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
public class IdmPingHealthProbe extends HealthProbe {

    private static final String TAG = "IDM Ping: ";

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
            if (IDM_ACTIVE.equals(MapUtils.getString(pingResponse, STATE))) {
                log.info(TAG + "success");
                return true;
            } else {
                log.error(TAG + "response did not contain expected value");
            }
        } catch (Exception e) {
            log.error(TAG +  e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
        }
        return false;
    }

    private String encode(String identity, String secret) {
        return Base64.getEncoder().encodeToString((identity + ":" + secret).getBytes());
    }
}
