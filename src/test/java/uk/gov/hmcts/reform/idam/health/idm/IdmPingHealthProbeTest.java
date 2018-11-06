package uk.gov.hmcts.reform.idam.health.idm;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdmPingHealthProbeTest {

    @Mock
    private IdmProvider idmProvider;

    @InjectMocks
    private IdmPingHealthProbe probe;

    @Test
    public void testProbe_success() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failUnexpectedState() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failMissingState() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("unexpected", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failEmptyResponse() {
        when(idmProvider.ping(anyString())).thenReturn(new HashMap<>());
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failNullResponse() {
        when(idmProvider.ping(anyString())).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failException() {
        when(idmProvider.ping(anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

}
