package uk.gov.hmcts.reform.idam.health.probe;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Slf4j
public abstract class HealthProbe {

    private String details;

    abstract public boolean probe();

    @Nonnull
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    public void setDetails(@Nullable String details) {
        log.info(details + " [" + getName() + "]");
        this.details = details;
    }
}
