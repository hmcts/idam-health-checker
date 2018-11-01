package uk.gov.hmcts.reform.idam.health.probe;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduledHealthProbe {

    private final HealthProbe healthProbe;
    private final Long freshnessInterval;

    private Status status = Status.UNKNOWN;
    private LocalDateTime statusDateTime;

    public ScheduledHealthProbe(
            HealthProbe healthProbe,
            Long freshnessInterval,
            Long checkInterval) {
        this.healthProbe = healthProbe;
        this.freshnessInterval = freshnessInterval;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ScheduledHealthProbe.this.refresh();
            }
        };

        Timer timer = new Timer(healthProbe.getClass().getName(), true);
        timer.scheduleAtFixedRate(timerTask, 0, checkInterval);
    }

    public boolean isOkay() {
        return status == Status.UP
                && LocalDateTime.now().isBefore(statusDateTime.plus(freshnessInterval, ChronoUnit.MILLIS));
    }

    private void refresh() {
        boolean probeResult = this.healthProbe.probe();
        this.status = probeResult ? Status.UP : Status.DOWN;
        this.statusDateTime = LocalDateTime.now();
    }
}
