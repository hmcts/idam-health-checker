package uk.gov.hmcts.reform.idam.health.probe;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledHealthProbeIndicatorTest {

    private static final long EPOCH_1AM = 3600;

    @Mock
    private HealthProbe healthProbe;

    @Mock
    private TaskScheduler taskScheduler;

    private ScheduledHealthProbeIndicator strictScheduledHealthProbe;
    private ScheduledHealthProbeIndicator ignoringScheduledHealthProbe;
    private ScheduledHealthProbeIndicator ignoringOnceReadyScheduledHealthProbe;

    @Before
    public void setup() {
        strictScheduledHealthProbe = new ScheduledHealthProbeIndicator(
                healthProbe, HealthProbeFailureHandling.MARK_AS_DOWN, taskScheduler, 40000L, 30000L);
        ignoringScheduledHealthProbe = new ScheduledHealthProbeIndicator(
                healthProbe, HealthProbeFailureHandling.IGNORE, taskScheduler, 40000L, 30000L);
        ignoringOnceReadyScheduledHealthProbe = new ScheduledHealthProbeIndicator(
                healthProbe, HealthProbeFailureHandling.IGNORE_ONCE_READY, taskScheduler, 40000L, 30000L);


        when(healthProbe.getName()).thenReturn("testprobe");
        verify(taskScheduler, times(3)).scheduleWithFixedDelay(any(Runnable.class), anyLong());
    }

    @Test
    public void testIsOkay_success() {
        when(healthProbe.probe()).thenReturn(true);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        assertThat(strictScheduledHealthProbe.isOkay(), is(true));
    }

    @Test
    public void testIsOkay_successBeforeRefresh() {
        when(healthProbe.probe()).thenReturn(true);
        assertThat(strictScheduledHealthProbe.isOkay(), is(true));
    }

    @Test
    public void testIsOkay_successNotExpired() {
        when(healthProbe.probe()).thenReturn(true);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.refresh();
        assertThat(strictScheduledHealthProbe.isOkay(), is(true));
        verify(healthProbe, times(1)).probe();
    }

    @Test
    public void testIsOkay_failNotFresh() {
        when(healthProbe.probe()).thenReturn(true);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 41), ZoneId.systemDefault()));
        assertThat(strictScheduledHealthProbe.isOkay(), is(false));
    }

    @Test
    public void testIsOkay_failDown() {
        when(healthProbe.probe()).thenReturn(false);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        assertThat(strictScheduledHealthProbe.isOkay(), is(false));
    }

    @Test
    public void testIsOkay_failStatusUnknown() {
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        assertThat(strictScheduledHealthProbe.isOkay(), is(false));
    }

    @Test
    public void testIsOkay_ignoreDown() {
        when(healthProbe.probe()).thenReturn(true);
        ignoringScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        ignoringScheduledHealthProbe.refresh();
        when(healthProbe.probe()).thenReturn(false);
        ignoringScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 31), ZoneId.systemDefault()));
        ignoringScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        verify(healthProbe, times(2)).probe();
    }

    @Test
    public void testIsOkay_ignoreDownForIgnoreOnceReady() {
        when(healthProbe.probe()).thenReturn(true);
        ignoringOnceReadyScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        ignoringOnceReadyScheduledHealthProbe.refresh();
        when(healthProbe.probe()).thenReturn(false);
        ignoringOnceReadyScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 31), ZoneId.systemDefault()));
        ignoringOnceReadyScheduledHealthProbe.refresh();
        assertThat(ignoringOnceReadyScheduledHealthProbe.isOkay(), is(true));
        verify(healthProbe, times(2)).probe();
    }

    @Test
    public void testIsOkay_alwaysSuccessWhenIgnoreProbe() {
        when(healthProbe.probe()).thenReturn(false);
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        when(healthProbe.probe()).thenReturn(true);
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        ignoringScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        verify(healthProbe, times(3)).probe();
    }

    @Test
    public void testIsOkay_returnDownAtStartupForIgnoreOnceReady() {
        when(healthProbe.probe()).thenReturn(false);
        assertThat(ignoringOnceReadyScheduledHealthProbe.isOkay(), is(false));
        when(healthProbe.probe()).thenReturn(true);
        assertThat(ignoringOnceReadyScheduledHealthProbe.isOkay(), is(true));
        ignoringOnceReadyScheduledHealthProbe.refresh();
        assertThat(ignoringOnceReadyScheduledHealthProbe.isOkay(), is(true));
        verify(healthProbe, times(3)).probe();
    }

    @Test
    public void testIsOkay_successWhenNotFreshAndIgnore() {
        when(healthProbe.probe()).thenReturn(true);
        ignoringScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        ignoringScheduledHealthProbe.changeClock(Clock.offset(Clock.systemDefaultZone(),Duration.ofHours(24)));
        ignoringScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
    }

    @Test
    public void testIsOkay_successAfterFailure() {
        when(healthProbe.probe()).thenReturn(true);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
        when(healthProbe.probe()).thenReturn(false);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 60), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        assertThat(strictScheduledHealthProbe.isOkay(), is(false));
        when(healthProbe.probe()).thenReturn(true);
        strictScheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        strictScheduledHealthProbe.refresh();
        assertThat(ignoringScheduledHealthProbe.isOkay(), is(true));
    }

    @Test
    public void testHealth_up() {
        strictScheduledHealthProbe.setStatus(Status.UP);
        Health health = strictScheduledHealthProbe.health();
        assertThat(health.getStatus(), is(org.springframework.boot.actuate.health.Status.UP));
    }

    @Test
    public void testHealth_unknown() {
        strictScheduledHealthProbe.setStatus(Status.UNKNOWN);
        when(healthProbe.getDetails()).thenReturn(null);
        Health health = strictScheduledHealthProbe.health();
        assertThat(health.getStatus(), is(org.springframework.boot.actuate.health.Status.UNKNOWN));
        assertThat(health.getDetails(), is(anEmptyMap()));
    }

    @Test
    public void testHealth_unknownWithDetails() {
        strictScheduledHealthProbe.setStatus(Status.UNKNOWN);
        when(healthProbe.getDetails()).thenReturn("test-error");
        Health health = strictScheduledHealthProbe.health();
        assertThat(health.getStatus(), is(org.springframework.boot.actuate.health.Status.UNKNOWN));
        assertThat(health.getDetails().get("testprobe"), is("test-error"));
    }

    @Test
    public void testHealth_down() {
        strictScheduledHealthProbe.setStatus(Status.DOWN);
        when(healthProbe.getDetails()).thenReturn(null);
        Health health = strictScheduledHealthProbe.health();
        assertThat(health.getStatus(), is(org.springframework.boot.actuate.health.Status.DOWN));
        assertThat(health.getDetails(), is(anEmptyMap()));
    }

    @Test
    public void testHealth_downWithDetails() {
        strictScheduledHealthProbe.setStatus(Status.DOWN);
        when(healthProbe.getDetails()).thenReturn("test-error");
        Health health = strictScheduledHealthProbe.health();
        assertThat(health.getStatus(), is(org.springframework.boot.actuate.health.Status.DOWN));
        assertThat(health.getDetails().get("testprobe"), is("test-error"));
    }

}
