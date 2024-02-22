package uk.gov.hmcts.reform.idam.health.userstore;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("userstore")
@Slf4j
public class UserStoreReadyHealthProbe extends HealthProbe {

    private final UserStoreProvider userStoreProvider;

    public UserStoreReadyHealthProbe(UserStoreProvider userStoreProvider) {
        this.userStoreProvider = userStoreProvider;
    }

    @Override
    public boolean probe() {
        try {
            userStoreProvider.healthy();
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
