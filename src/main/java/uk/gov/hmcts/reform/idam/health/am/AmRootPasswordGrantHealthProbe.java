package uk.gov.hmcts.reform.idam.health.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
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
public class AmRootPasswordGrantHealthProbe extends HealthProbe {

    private static final String ACCESS_TOKEN = "access_token";

    private final AmProvider amProvider;
    private final AmHealthProbeProperties amHealthProbeProperties;

    public AmRootPasswordGrantHealthProbe(
            AmProvider amProvider,
            AmHealthProbeProperties healthProbeProperties) {
        this.amProvider = amProvider;
        this.amHealthProbeProperties = healthProbeProperties;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {
        try {
            Map<String, String> passwordGrantResponse = amProvider.rootPasswordGrantAccessToken(
                    null,
                    amHealthProbeProperties.getRootPasswordGrant().getAmUser(),
                    amHealthProbeProperties.getRootPasswordGrant().getAmPassword(),
                    amHealthProbeProperties.getRootPasswordGrant().getClientId(),
                    "default",
                    amHealthProbeProperties.getRootPasswordGrant().getClientScope());

            if (MapUtils.isNotEmpty(passwordGrantResponse) && passwordGrantResponse.containsKey(ACCESS_TOKEN)) {
                return handleSuccess();
            } else {
                return handleError("response did not contain expected value");
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

}
