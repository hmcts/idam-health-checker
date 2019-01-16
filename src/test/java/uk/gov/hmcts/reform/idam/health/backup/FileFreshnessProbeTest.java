package uk.gov.hmcts.reform.idam.health.backup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileFreshnessProbeTest {

    private static final long EPOCH_1AM = 3600;

    @Mock
    private FileFreshnessProbe.FileSystemInfo fileSystemInfo;

    private FileFreshnessProbe probe = new FileFreshnessProbe("test", "/test-path", 10000L);

    @Before
    public void setup() {
        probe.setFileSystemInfo(fileSystemInfo);
        probe.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
    }

    @Test
    public void testProbe_NothingAtPath() {
        when(fileSystemInfo.exists(any())).thenReturn(false);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_FileIsOkay() throws IOException {
        when(fileSystemInfo.exists(any())).thenReturn(true);
        when(fileSystemInfo.lastModifiedTime(any())).thenReturn(LocalDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_1AM).minus(2, ChronoUnit.SECONDS), ZoneId.systemDefault()));
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_FileIsOld() throws IOException {
        when(fileSystemInfo.exists(any())).thenReturn(true);
        when(fileSystemInfo.lastModifiedTime(any())).thenReturn(LocalDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_1AM).minus(12, ChronoUnit.SECONDS), ZoneId.systemDefault()));
        assertThat(probe.probe(), is(false));
    }

}
