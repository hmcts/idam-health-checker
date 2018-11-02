package uk.gov.hmcts.reform.idam.health.probe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("optimist")
public class OptimisticHealthProbe implements HealthProbe {

    @Override
    public boolean probe() {
        return true;
    }
}
