package uk.gov.hmcts.reform.idam.health.probe;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class BaseHealthProbe implements HealthProbe {

    private LocalDateTime lastExecutionTime;
    private Status currentStatus;
    private Long expiryTimeMillis;

    public BaseHealthProbe(Long expiryTimeMillis) {
        this.expiryTimeMillis = expiryTimeMillis;
        this.currentStatus = Status.UNKNOWN;
    }

    @Override
    public boolean isOkay() {
        return currentStatus == Status.UP
                && LocalDateTime.now().isBefore(lastExecutionTime.plus(expiryTimeMillis, ChronoUnit.MILLIS));
    }

    protected void setStatus(Status status) {
        currentStatus = status;
        lastExecutionTime = LocalDateTime.now();
    }

}
