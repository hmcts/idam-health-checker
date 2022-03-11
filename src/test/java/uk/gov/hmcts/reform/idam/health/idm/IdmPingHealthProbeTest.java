package uk.gov.hmcts.reform.idam.health.idm;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdmPingHealthProbeTest {

    @Mock
    private IdmProvider idmProvider;

    @InjectMocks
    private IdmPingHealthProbe probe;

    /**
     * @verifies pass when ping is ok
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldPassWhenPingIsOk() throws Exception {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        assertThat(probe.probe(), is(true));
    }

    /**
     * @verifies fail when ping state is unexpected
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenPingStateIsUnexpected() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ping state is missing
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenPingStateIsMissing() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("unexpected", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ping response is empty
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenPingResponseIsEmpty() {
        when(idmProvider.ping(anyString())).thenReturn(new HashMap<>());
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ping response is null
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenPingResponseIsNull() {
        when(idmProvider.ping(anyString())).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ping throws exception
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenPingThrowsException() {
        when(idmProvider.ping(anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

}
