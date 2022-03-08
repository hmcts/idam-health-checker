package uk.gov.hmcts.reform.idam.health.probe;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        this.details = details;
    }

    public abstract Logger getLogger();

    public boolean handleSuccess() {
        getLogger().info("{}: Success", getName());
        return true;
    }

    public boolean handleError(String message) {
        getLogger().warn("{}: {}", getName(), message);
        setDetails(message);
        return false;
    }

    public boolean handleException(Throwable t) {
        getLogger().error("{}: {}: [{}]", getName(), t.getMessage(), t.getClass().getSimpleName());
        setDetails(t.getMessage() + "[" + t.getClass().getSimpleName() + "]");
        return false;
    }
}
