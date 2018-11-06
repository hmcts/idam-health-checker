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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledHealthProbeTest {

    private static final long EPOCH_1AM = 3600;
    @Mock
    private HealthProbe healthProbe;

    @Mock
    private TaskScheduler taskScheduler;

    private ScheduledHealthProbeExecutor scheduledHealthProbe;

    @Before
    public void setup() {
        scheduledHealthProbe = new ScheduledHealthProbeExecutor(healthProbe, taskScheduler, 40000L, 30000L);
        verify(taskScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong());
    }

    @Test
    public void testIsOkay_success() {
        when(healthProbe.probe()).thenReturn(true);
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        scheduledHealthProbe.refresh();
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        assertThat(scheduledHealthProbe.isOkay(), is(true));
    }

    @Test
    public void testIsOkay_failNotFresh() {
        when(healthProbe.probe()).thenReturn(true);
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        scheduledHealthProbe.refresh();
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 41), ZoneId.systemDefault()));
        assertThat(scheduledHealthProbe.isOkay(), is(false));
    }

    @Test
    public void testIsOkay_failDown() {
        when(healthProbe.probe()).thenReturn(false);
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        scheduledHealthProbe.refresh();
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM + 10), ZoneId.systemDefault()));
        assertThat(scheduledHealthProbe.isOkay(), is(false));
    }

    @Test
    public void testIsOkay_failStatusUnknown() {
        scheduledHealthProbe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        assertThat(scheduledHealthProbe.isOkay(), is(false));
    }

}
