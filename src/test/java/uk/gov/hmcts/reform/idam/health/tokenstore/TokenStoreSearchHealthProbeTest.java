package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenStoreSearchHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @InjectMocks
    private TokenStoreSearchHealthProbe probe;

    @Test
    @SuppressWarnings("unchecked")
    public void testProbe_success() {
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any(AttributesMapper.class))).thenReturn(Collections.singletonList("test-response"));
        assertThat(probe.probe(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProbe_failEmptyResponse() {
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any(AttributesMapper.class))).thenReturn(Collections.emptyList());
        assertThat(probe.probe(), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProbe_failNullResponse() {
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any(AttributesMapper.class))).thenReturn(null);
        assertThat(probe.probe(), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProbe_failException() {
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any(AttributesMapper.class))).thenThrow(new BadLdapGrammarException("Fail"));
        assertThat(probe.probe(), is(false));
    }

}
