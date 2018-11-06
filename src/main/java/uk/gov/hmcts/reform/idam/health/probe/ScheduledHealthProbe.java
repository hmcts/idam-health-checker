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

    /*
    public ScheduledHealthProbe(
            HealthProbe healthProbe,
            Long freshnessInterval,
            Long checkInterval) {
        this(healthProbe, freshnessInterval, checkInterval, , (probe, scheduleInterval) -> {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    probe.refresh();
                }
            };
            Timer timer = new Timer(healthProbe.getClass().getName(), true);
            timer.scheduleAtFixedRate(timerTask, 0, scheduleInterval);
        });
    }

    protected ScheduledHealthProbe(HealthProbe healthProbe,
                                   Long freshnessInterval,
                                   Long checkInterval,
                                   Clock clock,
                                   HealthProbeScheduling scheduling) {
        this.healthProbe = healthProbe;
        this.freshnessInterval = freshnessInterval;
        this.clock = clock;
        scheduling.schedule(this, checkInterval);
    }
    */

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
