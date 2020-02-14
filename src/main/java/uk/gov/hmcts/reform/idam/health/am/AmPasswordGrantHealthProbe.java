package uk.gov.hmcts.reform.idam.health.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.AgentProperties;
import uk.gov.hmcts.reform.idam.health.props.ProbeUserProperties;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("am")
@Slf4j
public class AmPasswordGrantHealthProbe extends HealthProbe {

    private static final String TAG = "AM PasswordGrant: ";

    private static final String GRANT_TYPE = "password";
    private static final String ACCESS_TOKEN = "access_token";

    private final AmProvider amProvider;
    private final AmHealthProbeProperties amHealthProbeProperties;
    private final ProbeUserProperties probeUserProperties;
    private final String authorization;

    public AmPasswordGrantHealthProbe(
            AmProvider amProvider,
            AmHealthProbeProperties healthProbeProperties,
            ProbeUserProperties probeUserProperties,
            AgentProperties agentProperties) {
        this.amProvider = amProvider;
        this.amHealthProbeProperties = healthProbeProperties;
        this.probeUserProperties = probeUserProperties;

        this.authorization = "Basic " + encode(agentProperties.getName(), agentProperties.getSecret());
    }

    @Override
    public boolean probe() {
        try {
            Map<String, String> passwordGrantResponse = amProvider.passwordGrantAccessToken(
                    GRANT_TYPE,
                    amHealthProbeProperties.getIdentity().getHost(),
                    authorization,
                    probeUserProperties.getUsername(),
                    probeUserProperties.getPassword(),
                    amHealthProbeProperties.getIdentity().getScope());
            if (MapUtils.isNotEmpty(passwordGrantResponse) && passwordGrantResponse.containsKey(ACCESS_TOKEN)) {
                log.info(TAG + "success");
                return true;
            } else {
                setDetails(TAG + "response did not contain expected value");
            }
        } catch (Exception e) {
            setDetails(TAG + e.getMessage());
        }
        return false;
    }

    private String encode(String identity, String secret) {
        return Base64.getEncoder().encodeToString((identity + ":" + secret).getBytes());
    }
}
