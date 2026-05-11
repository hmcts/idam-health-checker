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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AMReadyHealthProbeTest {

    @Mock
    private AmProvider amProvider;

    @Mock
    private Response response;

    @InjectMocks
    private AMReadyHealthProbe probe;

    @Test
    public void testProbeSuccess() {
        when(response.status()).thenReturn(HttpStatus.OK.value());
        when(amProvider.healthReady()).thenReturn(response);

        assertThat(probe.probe(), is(true));

        verify(response).close();
    }

    @Test
    public void testProbeFailUnexpectedResponse() {
        when(response.status()).thenReturn(HttpStatus.I_AM_A_TEAPOT.value());
        when(amProvider.healthReady()).thenReturn(response);

        assertThat(probe.probe(), is(false));

        verify(response).close();
    }

    @Test
    public void testProbeFailException() {
        when(amProvider.healthReady()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThat(probe.probe(), is(false));
    }
}
