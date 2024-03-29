package uk.gov.hmcts.reform.idam.health.tokenstore;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

@Component
@Profile("tokenstore")
@Slf4j
public class TokenStoreAliveHealthProbe extends HealthProbe {

    private final TokenStoreProvider tokenStoreProvider;

    public TokenStoreAliveHealthProbe(TokenStoreProvider tokenStoreProvider) {
        this.tokenStoreProvider = tokenStoreProvider;
    }

    @Override
    public boolean probe() {
        try {
            tokenStoreProvider.alive();
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
