package uk.gov.hmcts.reform.idam.health.am;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmIsAliveHealthProbeTest {

    @Mock
    private AmProvider amProvider;

    @InjectMocks
    private AmIsAliveHealthProbe probe;

    @Test
    public void testProbe_success() {
        when(amProvider.isAlive()).thenReturn("Server is ALIVE!");
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProve_failUnexpectedResponse() {
        when(amProvider.isAlive()).thenReturn("Unexpected response");
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProve_failEmptyResponse() {
        when(amProvider.isAlive()).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProve_failException() {
        when(amProvider.isAlive()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

}
