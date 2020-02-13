package uk.gov.hmcts.reform.idam.health.am;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import javax.annotation.Nullable;

@Component
@Profile("am")
@Slf4j
public class AmIsAliveHealthProbe implements HealthProbe {

    private static final String TAG = "AM IsAlive: ";

    private static final String ALIVE = "ALIVE";

    private final AmProvider amProvider;

    private String details = null;

    public AmIsAliveHealthProbe(AmProvider amProvider) {
        this.amProvider = amProvider;
    }

    @Override
    public boolean probe() {
        try {
            System.out.println("amProvider.isAlive()");
            String isAliveResponse = amProvider.isAlive();
            System.out.println(isAliveResponse);
            if (StringUtils.contains(isAliveResponse, ALIVE)) {
                log.info(TAG + "success");
                return true;
            } else {
                String msg = TAG + "response did not contain expected value";
                log.error(msg);
                details = msg;
            }
        } catch (Exception e) {
            String msg = TAG + e.getMessage() + " [" + e.getClass().getSimpleName() + "]";
            log.error(msg);
            details = msg;
        }
        return false;
    }

    @Nullable
    @Override
    public String getDetails() {
        return details;
    }
}
