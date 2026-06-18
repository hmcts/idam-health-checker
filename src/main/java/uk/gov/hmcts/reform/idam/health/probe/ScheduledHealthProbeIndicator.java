package uk.gov.hmcts.reform.idam.health.probe;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

@Slf4j
public class ScheduledHealthProbeIndicator implements HealthProbeIndicator, HealthIndicator {

    private final HealthProbe healthProbe;
    private final Duration freshnessInterval;
    private final HealthProbeFailureHandling failureHandling;
    private final TaskScheduler taskScheduler;
    private final Duration checkInterval;
    private Clock clock;

    private Status status;
    private LocalDateTime statusDateTime;
    private Instant lastChecked;
    private Instant lastStatusChange;
    private Instant lastDetailUpdate;

    private static final EnumSet<Status> REQUIRE_PROBE_STATES = EnumSet.of(Status.OUT_OF_SERVICE, Status.UNKNOWN);

    public ScheduledHealthProbeIndicator(
            HealthProbe healthProbe,
            HealthProbeFailureHandling failureHandling,
            TaskScheduler taskScheduler,
            Duration freshnessInterval,
            Duration checkInterval) {
        this.healthProbe = healthProbe;
        this.failureHandling = failureHandling;
        this.taskScheduler = taskScheduler;
        this.freshnessInterval = freshnessInterval;
        this.checkInterval = checkInterval;
        this.status = failureHandling == HealthProbeFailureHandling.IGNORE ? Status.UNKNOWN: Status.OUT_OF_SERVICE;
        this.clock = Clock.systemDefaultZone();
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
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
        LocalDateTime now = LocalDateTime.now(clock);
        boolean probeHasExpired = REQUIRE_PROBE_STATES.contains(status) || now
                .isAfter(statusDateTime.plus(Math.round(0.5 * freshnessInterval.toMillis()), ChronoUnit.MILLIS));
        if (status == Status.UP && !probeHasExpired) {
            return;
        }

        boolean probeResult = this.healthProbe.probe();
        Instant checkedAt = clock.instant();
        String currentDetails = this.healthProbe.getDetails();

        this.lastChecked = checkedAt;
        updateDetailTimestamp(currentDetails, probeResult, checkedAt);

        if (probeResult || failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            Status newStatus = probeResult ? Status.UP : Status.DOWN;
            updateStatusTimestamp(this.status, newStatus, checkedAt);
            if (this.status != newStatus) {
                if (Status.DOWN.equals(newStatus)) {
                    log.error("{}: Status changing from {} to {}", this.healthProbe.getName(), this.status, newStatus);
                } else {
                    log.info("{}: Status changing from {} to {}", this.healthProbe.getName(), this.status, newStatus);
                }
            }

            this.status = newStatus;
            this.statusDateTime = LocalDateTime.ofInstant(checkedAt, clock.getZone());
        } else {
            updateStatusTimestamp(this.status, this.status, checkedAt);
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

    private void updateDetailTimestamp(String currentDetails, boolean probeResult, Instant checkedAt) {
        if (currentDetails == null) {
            this.lastDetailUpdate = null;
        } else if (!probeResult) {
            this.lastDetailUpdate = checkedAt;
        }
    }

    private void updateStatusTimestamp(Status previousStatus, Status currentStatus, Instant checkedAt) {
        if (lastStatusChange == null || previousStatus != currentStatus) {
            this.lastStatusChange = checkedAt;
        }
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
        String details = healthProbe.getDetails();
        if (details != null) {
            builder.withDetail(healthProbe.getName(), details);
        }
        addTimestampDetail(builder, "lastChecked", lastChecked);
        addTimestampDetail(builder, "lastStatusChange", lastStatusChange);
        addTimestampDetail(builder, "lastDetailUpdate", lastDetailUpdate);
        return builder.build();
    }

    private void addTimestampDetail(Health.Builder builder, String name, Instant timestamp) {
        if (timestamp != null) {
            builder.withDetail(name, timestamp.toString());
        }
    }
}
