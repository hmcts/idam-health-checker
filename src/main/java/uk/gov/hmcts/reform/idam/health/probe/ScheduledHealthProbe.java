package uk.gov.hmcts.reform.idam.health.probe;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduledHealthProbe {

    private final HealthProbe healthProbe;
    private final Long freshnessInterval;
    private Clock clock;

    private Status status = Status.UNKNOWN;
    private LocalDateTime statusDateTime;

    public ScheduledHealthProbe(HealthProbe healthProbe, TaskScheduler taskScheduler, Long freshnessInterval, Long checkInterval) {
        this.healthProbe = healthProbe;
        this.freshnessInterval = freshnessInterval;
        this.clock = Clock.systemDefaultZone();
        taskScheduler.scheduleWithFixedDelay(() -> refresh(), checkInterval);
    }

    public boolean isOkay() {
        return status == Status.UP
                && LocalDateTime.now(clock).isBefore(statusDateTime.plus(freshnessInterval, ChronoUnit.MILLIS));
    }

    protected void refresh() {
        boolean probeResult = this.healthProbe.probe();
        this.status = probeResult ? Status.UP : Status.DOWN;
        this.statusDateTime = LocalDateTime.now(clock);
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

}
