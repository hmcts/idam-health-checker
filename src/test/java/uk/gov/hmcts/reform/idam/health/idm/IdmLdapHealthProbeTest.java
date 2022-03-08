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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdmLdapHealthProbeTest {

    @Mock
    private IdmProvider idmProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IdmHealthProbeProperties idmHealthProbeProperties;

    private IdmLdapHealthProbe probe;

    @Before
    public void setup() {
        when(this.idmHealthProbeProperties.getLdapCheck().getUsername()).thenReturn("username");
        when(this.idmHealthProbeProperties.getLdapCheck().getPassword()).thenReturn("password");
        probe = new IdmLdapHealthProbe(idmProvider, idmHealthProbeProperties);
    }

    /**
     * @verifies pass when ldap is ok
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldPassWhenLdapIsOk() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("enabled", true));
        assertThat(probe.probe(), is(true));
    }

    /**
     * @verifies fail when ldap enabled is unexpected
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapEnabledIsUnexpected() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("enabled", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap enabled is missing
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapEnabledIsMissing() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(ImmutableMap.of("unexpected", "UNEXPECTED"));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap response is empty
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapResponseIsEmpty() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(new HashMap<>());
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap response is null
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapResponseIsNull() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail when ldap throws exception
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailWhenLdapThrowsException() throws Exception {
        when(idmProvider.checkLdap(anyString(), anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThat(probe.probe(), is(false));
    }

    /**
     * @verifies fail if ldap check is missing credentials
     * @see IdmLdapHealthProbe#probe()
     */
    @Test
    public void probe_shouldFailIfLdapCheckIsMissingCredentials() throws Exception {
        IdmHealthProbeProperties testProperties = mock(IdmHealthProbeProperties.class, Answers.RETURNS_DEEP_STUBS);
        when(testProperties.getLdapCheck().getUsername()).thenReturn(null);
        when(testProperties.getLdapCheck().getPassword()).thenReturn(null);
        IdmLdapHealthProbe testProbe = new IdmLdapHealthProbe(idmProvider, testProperties);
        assertThat(testProbe.probe(), is(false));
        assertThat(testProbe.getDetails(), is("IDM ldap check has no credentials set"));
    }
}
