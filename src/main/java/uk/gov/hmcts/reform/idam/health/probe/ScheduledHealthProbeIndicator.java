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

    private LocalDateTime statusDateTime;
    private ProbeObservation currentObservation;

    private static final EnumSet<Status> REQUIRE_PROBE_STATES = EnumSet.of(Status.OUT_OF_SERVICE, Status.UNKNOWN);

    private record ProbeObservation(
            Status status,
            String details,
            Instant lastChecked,
            Instant lastStatusChange,
            Instant lastDetailUpdate) {
    }

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
        Status initialStatus = failureHandling == HealthProbeFailureHandling.IGNORE ? Status.UNKNOWN: Status.OUT_OF_SERVICE;
        this.currentObservation = new ProbeObservation(initialStatus, null, null, null, null);
        this.clock = Clock.systemDefaultZone();
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        taskScheduler.scheduleWithFixedDelay(this::refresh, checkInterval);
    }

    @Override
    public boolean isOkay() {
        Status status = currentObservation.status();
        if (REQUIRE_PROBE_STATES.contains(status)) {
            return runProbeAndRecordObservation() || failureHandling == HealthProbeFailureHandling.IGNORE;
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
        Status previousStatus = currentObservation.status();
        LocalDateTime now = LocalDateTime.now(clock);
        boolean probeHasExpired = REQUIRE_PROBE_STATES.contains(previousStatus) || statusDateTime == null || now
                .isAfter(statusDateTime.plus(Math.round(0.5 * freshnessInterval.toMillis()), ChronoUnit.MILLIS));
        if (previousStatus == Status.UP && !probeHasExpired) {
            return;
        }

        runProbeAndRecordObservation();
    }

    private boolean runProbeAndRecordObservation() {
        Status previousStatus = currentObservation.status();
        boolean probeResult = this.healthProbe.probe();
        Instant checkedAt = clock.instant();
        String currentDetails = this.healthProbe.getDetails();
        Status newStatus = previousStatus;

        if (probeResult || failureHandling == HealthProbeFailureHandling.MARK_AS_DOWN) {
            newStatus = probeResult ? Status.UP : Status.DOWN;
            if (previousStatus != newStatus) {
                if (Status.DOWN.equals(newStatus)) {
                    log.error("{}: Status changing from {} to {}", this.healthProbe.getName(), previousStatus, newStatus);
                } else {
                    log.info("{}: Status changing from {} to {}", this.healthProbe.getName(), previousStatus, newStatus);
                }
            }

            this.statusDateTime = LocalDateTime.ofInstant(checkedAt, clock.getZone());
        } else {
            log.warn("{}: probe failed, status {} unchanged", this.healthProbe.getName(), previousStatus);
        }

        recordObservation(newStatus, currentDetails, probeResult, checkedAt);
        return probeResult;
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

    @VisibleForTesting
    protected void setStatus(Status status) {
        this.currentObservation = new ProbeObservation(
                status,
                currentObservation.details(),
                currentObservation.lastChecked(),
                currentObservation.lastStatusChange(),
                currentObservation.lastDetailUpdate());
    }

    private void recordObservation(Status status, String details, boolean probeResult, Instant checkedAt) {
        Instant lastStatusChange = currentObservation.lastStatusChange();
        if (lastStatusChange == null || currentObservation.status() != status) {
            lastStatusChange = checkedAt;
        }

        Instant lastDetailUpdate = currentObservation.lastDetailUpdate();
        if (details == null) {
            lastDetailUpdate = null;
        } else if (!probeResult) {
            lastDetailUpdate = checkedAt;
        }

        this.currentObservation = new ProbeObservation(
                status,
                details,
                checkedAt,
                lastStatusChange,
                lastDetailUpdate);
    }

    @Override
    public Health health() {
        ProbeObservation observation = currentObservation;
        Health.Builder builder;
        if (observation.status() == Status.UP) {
            builder = Health.up();
        } else if (observation.status() == Status.UNKNOWN) {
            builder = Health.unknown();
        } else if (observation.status() == Status.OUT_OF_SERVICE) {
            builder = Health.outOfService();
        } else {
            builder = Health.down();
        }
        if (observation.details() != null) {
            builder.withDetail(healthProbe.getName(), observation.details());
        }
        addTimestampDetail(builder, "lastChecked", observation.lastChecked());
        addTimestampDetail(builder, "lastStatusChange", observation.lastStatusChange());
        addTimestampDetail(builder, "lastDetailUpdate", observation.lastDetailUpdate());
        return builder.build();
    }

    private void addTimestampDetail(Health.Builder builder, String name, Instant timestamp) {
        if (timestamp != null) {
            builder.withDetail(name, timestamp.toString());
        }
    }
}
