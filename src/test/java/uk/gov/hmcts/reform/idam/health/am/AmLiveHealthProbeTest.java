package uk.gov.hmcts.reform.idam.health.am;

import feign.Response;
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
public class AmLiveHealthProbeTest {

    @Mock
    private AmProvider amProvider;

    @Mock
    Response response;

    @InjectMocks
    private AmLiveHealthProbe probe;

    @Test
    public void testProbe_success() {
        when(response.status()).thenReturn(HttpStatus.OK.value());
        when(amProvider.healthLive()).thenReturn(response);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProve_failUnexpectedResponse() {
        when(response.status()).thenReturn(HttpStatus.I_AM_A_TEAPOT.value());
        when(amProvider.healthLive()).thenReturn(response);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProve_failException() {
        when(amProvider.healthLive()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

}
