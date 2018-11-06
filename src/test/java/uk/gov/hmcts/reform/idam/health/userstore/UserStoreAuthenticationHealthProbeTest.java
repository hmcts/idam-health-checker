package uk.gov.hmcts.reform.idam.health.userstore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.core.LdapTemplate;
import uk.gov.hmcts.reform.idam.health.props.ProbeUserProperties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserStoreAuthenticationHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private ProbeUserProperties probeUserProperties;

    private UserStoreAuthenticationHealthProbe probe;

    @Before
    public void setup() {
        when(probeUserProperties.getUsername()).thenReturn("test-username");
        when(probeUserProperties.getPassword()).thenReturn("test-password");
        probe = new UserStoreAuthenticationHealthProbe(ldapTemplate, probeUserProperties);
    }

    @Test
    public void testProbe_success() {
        when(ldapTemplate.authenticate(anyString(), anyString(), anyString())).thenReturn(true);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failAuthentication() {
        when(ldapTemplate.authenticate(anyString(), anyString(), anyString())).thenReturn(false);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failException() {
        when(ldapTemplate.authenticate(anyString(), anyString(), anyString())).thenThrow(new BadLdapGrammarException("Fail"));
        assertThat(probe.probe(), is(false));
    }
}
