package uk.gov.hmcts.reform.idam.health.userstore;

import lombok.CustomLog;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("userstore")
@CustomLog
public class UserStoreAliveHealthProbe extends HealthProbe {

    private final UserStoreProvider userStoreProvider;

    public UserStoreAliveHealthProbe(UserStoreProvider userStoreProvider) {
        this.userStoreProvider = userStoreProvider;
    }

    @Override
    public boolean probe() {
        try {
            userStoreProvider.alive();
            return handleSuccess();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
