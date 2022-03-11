package uk.gov.hmcts.reform.idam.health.probe;

import org.slf4j.Logger;


public abstract class HealthProbe {

    private String details;

    abstract public boolean probe();

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
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
