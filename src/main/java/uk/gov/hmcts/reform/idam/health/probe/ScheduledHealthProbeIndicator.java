package uk.gov.hmcts.reform.idam.health.probe;

import com.google.common.annotations.VisibleForTesting;
import lombok.CustomLog;
import org.springframework.scheduling.TaskScheduler;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@CustomLog
public class ScheduledHealthProbeIndicator implements HealthProbeIndicator {

    private final HealthProbe healthProbe;
    private final Long freshnessInterval;
    private final HealthProbeFailureHandling failureHandling;
    private Clock clock;

    private Status status = Status.UNKNOWN;
    private LocalDateTime statusDateTime;

    public ScheduledHealthProbeIndicator(
            HealthProbe healthProbe,
            HealthProbeFailureHandling failureHandling,
            TaskScheduler taskScheduler,
            Long freshnessInterval,
            Long checkInterval) {
        this.healthProbe = healthProbe;
        this.failureHandling = failureHandling;
        this.freshnessInterval = freshnessInterval;
        this.clock = Clock.systemDefaultZone();
        taskScheduler.scheduleWithFixedDelay(this::refresh, checkInterval);
    }

    @Override
    public boolean isOkay() {
        if (status == Status.UNKNOWN) {
            return this.healthProbe.probe() || failureHandling == HealthProbeFailureHandling.IGNORE;
        }

        if (failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            return status == Status.UP
                    && LocalDateTime.now(clock).isBefore(statusDateTime.plus(freshnessInterval, ChronoUnit.MILLIS));
        } else {
            log.info("{}: status evaluation ignored for this type of probe. failureHandling: {}, status: {}", healthProbe.getName(), failureHandling, status);
            return true;
        }
    }

    @Nullable
    @Override
    public String getDetails() {
        return healthProbe.getDetails();
    }

    protected void refresh() {
        boolean probeHasExpired = status == Status.UNKNOWN || LocalDateTime.now(clock)
                .isAfter(statusDateTime.plus(Math.round(0.75 * freshnessInterval), ChronoUnit.MILLIS));
        if (status == Status.UP && !probeHasExpired) {
            return;
        }

        boolean probeResult = this.healthProbe.probe();

        if (probeResult || failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            Status newStatus = probeResult ? Status.UP : Status.DOWN;
            if (this.status != newStatus) {
                if (Status.DOWN.equals(newStatus)) {
                    log.error("{}: Status changing from {} to {}", this.healthProbe.getName(), this.status, newStatus);
                } else {
                    log.info("{}: Status changing from {} to {}", this.healthProbe.getName(), this.status, newStatus);
                }
            }

            this.status = newStatus;
            this.statusDateTime = LocalDateTime.now(clock);
        } else if (failureHandling == HealthProbeFailureHandling.IGNORE) {
            log.warn("{}: DOWN state ignored", this.healthProbe.getName());
        }
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

}
