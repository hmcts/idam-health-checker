package uk.gov.hmcts.reform.idam.health.probe.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("am")
@Slf4j
public class AmPasswordGrantHealthStatus implements HealthStatus {

    private final AmHealthProbeProperties amHealthProbeProperties;

    public static final String GRANT_TYPE = "password";
    private static final String ACCESS_TOKEN = "access_token";

    private final AmProvider amProvider;
    private final String authorization;

    public AmPasswordGrantHealthStatus(AmProvider amProvider, AmHealthProbeProperties amHealthProbeProperties) {
        this.amProvider = amProvider;
        this.amHealthProbeProperties = amHealthProbeProperties;
        this.authorization = "Basic " + Base64.getEncoder().encodeToString(
                (amHealthProbeProperties.getIdentity().getClient() + ":" + amHealthProbeProperties.getIdentity().getClientSecret()).getBytes());
    }

    @Override
    public Status determineStatus() {
        try {
            Map<String, String> response = amProvider.passwordGrantAccessToken(
                    GRANT_TYPE,
                    amHealthProbeProperties.getIdentity().getHost(),
                    authorization,
                    amHealthProbeProperties.getIdentity().getUsername(),
                    amHealthProbeProperties.getIdentity().getPassword(),
                    amHealthProbeProperties.getIdentity().getScope());
            if (MapUtils.isNotEmpty(response) && response.containsKey(ACCESS_TOKEN)) {
                return Status.UP;
            }
        } catch (Exception e) {
            log.error("AM password: " + e.getMessage());
        }
        return Status.DOWN;
    }

}
