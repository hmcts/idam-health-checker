package uk.gov.hmcts.reform.idam.health.tokenstore;

import lombok.CustomLog;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("tokenstore")
@CustomLog
public class TokenStoreReadyHealthProbe extends HealthProbe {

    private final TokenStoreProvider tokenStoreProvider;

    public TokenStoreReadyHealthProbe(TokenStoreProvider tokenStoreProvider) {
        this.tokenStoreProvider = tokenStoreProvider;
    }

    @Override
    public boolean probe() {
        try {
            tokenStoreProvider.healthy();
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