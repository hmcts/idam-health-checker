package uk.gov.hmcts.reform.idam.health.am;

import feign.Response;
import lombok.CustomLog;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("am")
@CustomLog
public class AmLiveHealthProbe extends HealthProbe {

    private final AmProvider amProvider;

    public AmLiveHealthProbe(AmProvider amProvider) {
        this.amProvider = amProvider;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {
        try {
            Response rsp = amProvider.healthLive();
            if (rsp.status() == HttpStatus.SC_OK) {
                return handleSuccess();
            } else {
                return handleError("Unexpected response: " + rsp.status());
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

}
