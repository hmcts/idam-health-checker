package uk.gov.hmcts.reform.idam.health.probe;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

@Slf4j
public class ScheduledHealthProbeIndicator implements HealthProbeIndicator, HealthIndicator {

    private final HealthProbe healthProbe;
    private final Duration freshnessInterval;
    private final HealthProbeFailureHandling failureHandling;
    private Clock clock;

    private Status status;
    private LocalDateTime statusDateTime;

    private static final EnumSet<Status> REQUIRE_PROBE_STATES = EnumSet.of(Status.OUT_OF_SERVICE, Status.UNKNOWN);

    public ScheduledHealthProbeIndicator(
            HealthProbe healthProbe,
            HealthProbeFailureHandling failureHandling,
            TaskScheduler taskScheduler,
            Duration freshnessInterval,
            Duration checkInterval) {
        this.healthProbe = healthProbe;
        this.failureHandling = failureHandling;
        this.freshnessInterval = freshnessInterval;
        this.status = failureHandling == HealthProbeFailureHandling.IGNORE ? Status.UNKNOWN: Status.OUT_OF_SERVICE;
        this.clock = Clock.systemDefaultZone();
        taskScheduler.scheduleWithFixedDelay(this::refresh, checkInterval);
    }

    @Override
    public boolean isOkay() {
        if (REQUIRE_PROBE_STATES.contains(status)) {
            return this.healthProbe.probe() || failureHandling == HealthProbeFailureHandling.IGNORE;
        }

        if (failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            return status == Status.UP
                    && LocalDateTime.now(clock).isBefore(statusDateTime.plus(freshnessInterval));
        } else {
            log.debug("{}: status evaluation ignored for this type of probe. failureHandling: {}, status: {}", healthProbe.getName(), failureHandling, status);
            return true;
        }
    }

    protected void refresh() {
        boolean probeHasExpired = REQUIRE_PROBE_STATES.contains(status) || LocalDateTime.now(clock)
                .isAfter(statusDateTime.plus(Math.round(0.5 * freshnessInterval.toMillis()), ChronoUnit.MILLIS));
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
        } else {
            log.warn("{}: probe failed, status {} unchanged", this.healthProbe.getName(), this.status);
        }
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

    @VisibleForTesting
    protected void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Health health() {
        Health.Builder builder;
        if (status == Status.UP) {
            builder = Health.up();
        } else if (status == Status.UNKNOWN) {
            builder = Health.unknown();
        } else if (status == Status.OUT_OF_SERVICE) {
            builder = Health.outOfService();
        } else {
            builder = Health.down();
        }
        if (healthProbe.getDetails() != null) {
            builder.withDetail(healthProbe.getName(), healthProbe.getDetails());
        }
        return builder.build();
    }
}
