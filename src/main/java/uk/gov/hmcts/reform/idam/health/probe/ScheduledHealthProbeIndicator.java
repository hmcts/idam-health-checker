package uk.gov.hmcts.reform.idam.health.probe;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
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
        taskScheduler.scheduleWithFixedDelay(() -> refresh(), checkInterval);
    }

    @Override
    public boolean isOkay() {
        if (status == Status.UNKNOWN) {
            return this.healthProbe.probe();
        }
        return status == Status.UP
                && LocalDateTime.now(clock).isBefore(statusDateTime.plus(freshnessInterval, ChronoUnit.MILLIS));
    }

    protected void refresh() {
        boolean probeHasExpired = status == Status.UNKNOWN || LocalDateTime.now(clock)
                .isAfter(statusDateTime.plus(Math.round((3.0/4) * freshnessInterval), ChronoUnit.MILLIS));
        if (status == Status.UP && !probeHasExpired) {
            return;
        }

        boolean probeResult = this.healthProbe.probe();

        if (probeResult || failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            this.status = probeResult ? Status.UP : Status.DOWN;
            this.statusDateTime = LocalDateTime.now(clock);
        } else if (failureHandling == HealthProbeFailureHandling.IGNORE) {
            log.warn(this.healthProbe.getName() + " DOWN state ignored");
        }
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

}
