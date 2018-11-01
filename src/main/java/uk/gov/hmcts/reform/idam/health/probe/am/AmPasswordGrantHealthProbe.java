package uk.gov.hmcts.reform.idam.health.probe.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.RestHealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.env.AgentProperties;
import uk.gov.hmcts.reform.idam.health.probe.env.ProbeUserProperties;

import java.util.Map;

@Component
@Profile("am")
@Slf4j
public class AmPasswordGrantHealthProbe extends RestHealthProbe<Map<String, String>> {

    public static final String GRANT_TYPE = "password";
    private static final String ACCESS_TOKEN = "access_token";

    private final AmHealthProbeProperties amHealthProbeProperties;
    private final ProbeUserProperties probeUserProperties;
    private final AgentProperties agentProperties;
    private final AmProvider amProvider;

    private final String authorization;

    public AmPasswordGrantHealthProbe(AmHealthProbeProperties amHealthProbeProperties,
                                      ProbeUserProperties probeUserProperties,
                                      AgentProperties agentProperties,
                                      AmProvider amProvider) {
        super(amHealthProbeProperties.getPasswordGrant().getFreshnessInterval());
        this.amHealthProbeProperties = amHealthProbeProperties;
        this.amProvider = amProvider;
        this.probeUserProperties = probeUserProperties;
        this.agentProperties = agentProperties;
        this.authorization = "Basic " + encode(agentProperties.getName(), agentProperties.getSecret());
    }

    @Override
    protected Map<String, String> makeRestCall() {
        return amProvider.passwordGrantAccessToken(
                GRANT_TYPE,
                amHealthProbeProperties.getIdentity().getHost(),
                authorization,
                probeUserProperties.getUsername(),
                probeUserProperties.getPassword(),
                amHealthProbeProperties.getIdentity().getScope());
    }

    @Override
    protected boolean validateContent(Map<String, String> content) {
        return MapUtils.isNotEmpty(content) && content.containsKey(ACCESS_TOKEN);
    }

    @Override
    protected void handleException(Exception e) {
        log.error("AM Password Grant: " + e.getMessage());
    }

    @Scheduled(fixedDelayString = "${am.healthprobe.passwordgrant.check-interval}")
    public void amPasswordGrantTask() {
        log.info("Refreshing AM PasswordGrant");
        refresh();
    }
}
