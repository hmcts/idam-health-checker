package uk.gov.hmcts.reform.idam.health.probe;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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

    @Before
    public void setup() {
        strictScheduledHealthProbe = new ScheduledHealthProbeIndicator(
                healthProbe, HealthProbeFailureHandling.MARK_AS_DOWN, taskScheduler, 40000L, 30000L);
        ignoringScheduledHealthProbe = new ScheduledHealthProbeIndicator(
                healthProbe, HealthProbeFailureHandling.IGNORE, taskScheduler, 40000L, 30000L);

        verify(taskScheduler, times(2)).scheduleWithFixedDelay(any(Runnable.class), anyLong());
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
}
