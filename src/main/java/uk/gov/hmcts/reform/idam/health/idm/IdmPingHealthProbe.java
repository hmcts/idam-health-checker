package uk.gov.hmcts.reform.idam.health.idm;

import lombok.CustomLog;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.Map;

@Component
@Profile("idm")
@CustomLog
public class IdmPingHealthProbe extends HealthProbe {

    private static final String STATE = "state";
    private static final String IDM_ACTIVE = "ACTIVE_READY";

    private final IdmProvider idmProvider;

    public IdmPingHealthProbe(IdmProvider idmProvider) {
        this.idmProvider = idmProvider;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    /**
     * @should pass when ping is ok
     * @should fail when ping state is unexpected
     * @should fail when ping state is missing
     * @should fail when ping response is empty
     * @should fail when ping response is null
     * @should fail when ping throws exception
     */
    @Override
    public boolean probe() {
        try {
            final Map<String, String> pingResponse = idmProvider.ping();
            final boolean pingOk = IDM_ACTIVE.equals(MapUtils.getString(pingResponse, STATE));

            if (pingOk) {
                return handleSuccess();
            } else {
                return handleError("idm ping response did not contain expected value");
            }

        } catch (Exception e) {
            handleException(e);
        }

        return false;
    }

}
