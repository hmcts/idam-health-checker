package uk.gov.hmcts.reform.idam.health.userstore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import uk.gov.hmcts.reform.idam.health.props.ProbeUserProperties;

import javax.naming.directory.SearchControls;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        when(ldapTemplate.search(anyString(), anyString(), any(int.class), any(AttributesMapper.class))).thenReturn(singletonList("test-username"));
        probe = new UserStoreAuthenticationHealthProbe(ldapTemplate, probeUserProperties);
    }

    @Test
    public void testProbe_success() {
        when(ldapTemplate.authenticate(anyString(), anyString(), anyString())).thenReturn(true);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_userDoesNotExist() {
        when(ldapTemplate.search(anyString(), anyString(), any(int.class), any(AttributesMapper.class))).thenReturn(emptyList());
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
