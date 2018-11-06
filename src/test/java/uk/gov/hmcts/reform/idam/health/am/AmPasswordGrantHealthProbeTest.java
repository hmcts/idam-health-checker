package uk.gov.hmcts.reform.idam.health.am;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.idam.health.props.AgentProperties;
import uk.gov.hmcts.reform.idam.health.props.ProbeUserProperties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmPasswordGrantHealthProbeTest {

    @Mock
    private AmProvider amProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AmHealthProbeProperties amHealthProbeProperties;

    @Mock
    private ProbeUserProperties probeUserProperties;

    @Mock
    private AgentProperties agentProperties;

    private AmPasswordGrantHealthProbe probe;

    @Before
    public void setup() {
        when(amHealthProbeProperties.getIdentity().getHost()).thenReturn("test-host");
        when(amHealthProbeProperties.getIdentity().getScope()).thenReturn("test-scope");
        when(probeUserProperties.getUsername()).thenReturn("test-user");
        when(probeUserProperties.getPassword()).thenReturn("test-pass");
        when(agentProperties.getName()).thenReturn("test-client");
        when(agentProperties.getSecret()).thenReturn("test-secret");
        probe = new AmPasswordGrantHealthProbe(
                amProvider,
                amHealthProbeProperties,
                probeUserProperties,
                agentProperties
        );
    }

    @Test
    public void testProbe_success() {
        when(amProvider.passwordGrantAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("access_token", "test-token"));
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failUnexpectedResponse() {
        when(amProvider.passwordGrantAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("something_else", "unexpected-value"));
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failEmptyResponse() {
        when(amProvider.passwordGrantAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failException() {
        when(amProvider.passwordGrantAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }
}
