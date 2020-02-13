package uk.gov.hmcts.reform.idam.health.probe;

import javax.annotation.Nullable;

public interface HealthProbe {

    boolean probe();

    default String getName() {
        return this.getClass().getSimpleName();
    };

    @Nullable
    default String getDetails() {
        return null;
    }
}
