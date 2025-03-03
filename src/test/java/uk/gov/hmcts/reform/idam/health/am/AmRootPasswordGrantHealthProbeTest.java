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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmRootPasswordGrantHealthProbeTest {

    @Mock
    private AmProvider amProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AmHealthProbeProperties amHealthProbeProperties;


    private AmRootPasswordGrantHealthProbe probe;

    @Before
    public void setup() {
        when(amHealthProbeProperties.getRootPasswordGrant().getAmUser()).thenReturn("test-am-user");
        when(amHealthProbeProperties.getRootPasswordGrant().getAmPassword()).thenReturn("test-am-pass");
        when(amHealthProbeProperties.getRootPasswordGrant().getClientId()).thenReturn("test-client");
        when(amHealthProbeProperties.getRootPasswordGrant().getClientScope()).thenReturn("test-scope");
        probe = new AmRootPasswordGrantHealthProbe(
                amProvider,
                amHealthProbeProperties);
    }

    @Test
    public void testProbe_success() {
        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("access_token", "test-token"));
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failUnexpectedResponse() {
        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("something_else", "unexpected-value"));
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failEmptyResponse() {
        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failException() {
        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failThenPassException() {
        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
        assertThat(probe.probe(), is(false));

        when(amProvider.rootPasswordGrantAccessToken(isNull(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("access_token", "test-token"));
        assertThat(probe.probe(), is(true));
    }
}
