package uk.gov.hmcts.reform.idam.health.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LdapConnectionsHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    private LdapConnectionsHealthProbe probe;

    @BeforeEach
    public void setup() {
        probe = new LdapConnectionsHealthProbe("probeName", ldapTemplate);
    }

    @Test
    public void testProbe_successOneResult() {
        List<LdapConnectionsHealthProbe.ConnectionsInfo> searchResult = new ArrayList<>();
        searchResult.add(connectionsInfo("100"));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapConnectionsHealthProbe.ConnectionsContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successTwoResults() {
        List<LdapConnectionsHealthProbe.ConnectionsInfo> searchResult = new ArrayList<>();
        searchResult.add(connectionsInfo("100"));
        searchResult.add(connectionsInfo("200"));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapConnectionsHealthProbe.ConnectionsContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failNoResults() {
        when(ldapTemplate.search(any(LdapQuery.class), any(LdapConnectionsHealthProbe.ConnectionsContextMapper.class))).thenReturn(Collections.emptyList());
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failWithException() {
        when(ldapTemplate.search(any(LdapQuery.class), any(LdapConnectionsHealthProbe.ConnectionsContextMapper.class))).thenThrow(new RuntimeException());
        assertThat(probe.probe(), is(false));
    }

    private LdapConnectionsHealthProbe.ConnectionsInfo connectionsInfo(String connectionsCount) {
        LdapConnectionsHealthProbe.ConnectionsInfo connectionsInfo = new LdapConnectionsHealthProbe.ConnectionsInfo();
        connectionsInfo.dn = "dn";
        connectionsInfo.activeConnectionCount = connectionsCount;
        return connectionsInfo;
    }

}
