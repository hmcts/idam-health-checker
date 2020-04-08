package uk.gov.hmcts.reform.idam.health.am;

import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("am")
@CustomLog
public class AmIsAliveHealthProbe extends HealthProbe {

    private static final String TAG = "AM IsAlive: ";

    private static final String ALIVE = "ALIVE";

    private final AmProvider amProvider;

    public AmIsAliveHealthProbe(AmProvider amProvider) {
        this.amProvider = amProvider;
    }

    @Override
    public boolean probe() {
        try {
            String isAliveResponse = amProvider.isAlive();
            if (StringUtils.contains(isAliveResponse, ALIVE)) {
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
}
