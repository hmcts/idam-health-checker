package uk.gov.hmcts.reform.idam.health.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LdapReplicationHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private ConfigProperties configProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigProperties.Ldap ldapProperties;

    private LdapReplicationHealthProbe probe;

    @Before
    public void setup() {
        when(configProperties.getLdap()).thenReturn(ldapProperties);
        when(ldapProperties.getReplication().getMissingChangesThreshold()).thenReturn(0);
        when(ldapProperties.getReplication().getPendingUpdatesThreshold()).thenReturn(0);
        probe = new LdapReplicationHealthProbe(ldapTemplate, configProperties);
    }

    @Test
    public void testProbe_successTwoResultsNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 0, 0));
        searchResult.add(replicationInfo("normal", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationAttributeMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successOneResultNotNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("unexpected", 0, 0));
        searchResult.add(replicationInfo("normal", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationAttributeMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successNoneNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("unexpected", 0, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationAttributeMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failOneMissing() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 1, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationAttributeMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failOnePending() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 0, 1));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationAttributeMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    protected LdapReplicationHealthProbe.ReplicationInfo replicationInfo(String status, int missing, int pending) {
        LdapReplicationHealthProbe.ReplicationInfo info = new LdapReplicationHealthProbe.ReplicationInfo();
        info.status = status;
        info.missingChanges = missing;
        info.pendingUpdates = pending;
        return info;
    }

}
