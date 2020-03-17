package uk.gov.hmcts.reform.idam.health.idm;

import com.google.common.collect.ImmutableMap;
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

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IdmHealthProbeProperties idmHealthProbeProperties;

    @InjectMocks
    private IdmPingHealthProbe probe;

    @Before
    public void idmHealthProbePropertiesSetup() {
        when(this.idmHealthProbeProperties.getLdapCheck().getEnabled()).thenReturn(true);
        when(this.idmHealthProbeProperties.getLdapCheck().getUsername()).thenReturn("username");
        when(this.idmHealthProbeProperties.getLdapCheck().getPassword()).thenReturn("password");
    }

    /**
     * @verifies pass when ping and ldap are ok
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldPassWhenPingAndLdapAreOk() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("enabled", true));
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

    /**
     * @verifies fail when ldap enabled is false
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapEnabledIsFalse() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("enabled", false));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap enabled is unexpected
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapEnabledIsUnexpected() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("enabled", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap enabled is missing
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapEnabledIsMissing() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("unexpected", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap response is empty
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapResponseIsEmpty() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(new HashMap<>());
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap response is null
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapResponseIsNull() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap throws exception
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapThrowsException() {
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        when(idmProvider.checkLdap(anyString(), anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies pass when ping is ok and ldap check if configured to skip
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldPassWhenPingIsOkAndLdapCheckIfConfiguredToSkip() throws Exception {
        when(this.idmHealthProbeProperties.getLdapCheck().getEnabled()).thenReturn(false);
        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        assertThat(probe.probe(), is(true));
        verify(idmProvider, never()).checkLdap(anyString(), anyString());
    }

    /**
     * @verifies fail if ldap check is missing credentials
     * @see IdmPingHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailIfLdapCheckIsMissingCredentials() throws Exception {
        when(this.idmHealthProbeProperties.getLdapCheck().getUsername()).thenReturn(null);
        when(this.idmHealthProbeProperties.getLdapCheck().getPassword()).thenReturn(null);

        when(idmProvider.ping(anyString())).thenReturn(ImmutableMap.of("state", "ACTIVE_READY"));
        
        assertThat(probe.probe(), is(false));
    }
}
