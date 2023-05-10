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
import static uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe.ReplicationRecordType.LOCAL_DS;
import static uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe.ReplicationRecordType.LOCAL_RS;
import static uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe.ReplicationRecordType.LOCAL_RS_CONN_DS;

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
        when(ldapProperties.getReplication().getMissingUpdatesThreshold()).thenReturn(0);
        when(ldapProperties.getReplication().getDelayThreshold()).thenReturn(0);
        probe = new LdapReplicationHealthProbe("probeName", ldapTemplate, "ou=domain-name", configProperties);
    }

    @Test
    public void testProbe_successTwoResultsNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 0, 0));
        searchResult.add(replicationInfo("normal", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successOneResultNotNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("unexpected", 0, 0));
        searchResult.add(replicationInfo("normal", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successNoneNormal() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("unexpected", 0, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successOneMissing() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 1, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successOnePending() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo("normal", 0, 1));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successLocalDSReplayUpdatesOkay() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_DS, "normal", 1000, 1000, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failLocalDSReplayUpdatesMissing() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_DS, "normal", 1000, 1, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failLocalDSReplayUpdatesUnexpected() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_DS, "normal", 1, 1000, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_successLocalDSReplayUpdatesOkayUnexpectedStatus() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_DS, "unexpected", 1000, 1000, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successLocalRSConnDSApproximateDelayOkay() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_RS_CONN_DS, (String) null, 1000, 1000, 0));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failLocalRSConnDSApproximateDelayExceeded() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_RS_CONN_DS, (String) null, 1000, 1000, 1));
        searchResult.add(replicationInfo("unexpected", 0, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failLocalRSMissingChanges() {
        List<LdapReplicationHealthProbe.ReplicationInfo> searchResult = new ArrayList<>();
        searchResult.add(replicationInfo(LOCAL_RS, null, 1000, -1, -1, -1, 0));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapReplicationHealthProbe.ReplicationContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testParseReplayedUpdates_happyPath() {
        String replayedUpdates = "{\"count\":1,\"total\":22.000,\"mean_rate\":0.000,\"m1_rate\":0.000,\"m5_rate\":0.000,\"m15_rate\":0.002,\"mean\":21.955,\"min\":21.889,\"max\":22.020,\"stddev\":0.000,\"p50\":22.020,\"p75\":22.020,\"p95\":22.020,\"p98\":22.020,\"p99\":22.020,\"p999\":22.020,\"p9999\":22.020,\"p99999\":22.020}";
        assertThat(LdapReplicationHealthProbe.ReplicationContextMapper.parseReplayedUpdates(replayedUpdates), is(1));
    }

    @Test
    public void testParseReplayedUpdates_null() {
        assertThat(LdapReplicationHealthProbe.ReplicationContextMapper.parseReplayedUpdates(null), is(-1));
    }

    @Test
    public void testParseReplayedUpdates_invalid() {
        assertThat(LdapReplicationHealthProbe.ReplicationContextMapper.parseReplayedUpdates("hello"), is(-1));
    }

    protected LdapReplicationHealthProbe.ReplicationInfo replicationInfo(String status, int missing, int pending) {
        LdapReplicationHealthProbe.ReplicationInfo info = new LdapReplicationHealthProbe.ReplicationInfo();
        info.status = status;
        info.missingChanges = missing;
        info.pendingUpdates = pending;
        return info;
    }

    protected LdapReplicationHealthProbe.ReplicationInfo replicationInfo(LdapReplicationHealthProbe.ReplicationRecordType recordType, String status, int receivedUpdates, int replayedUpdates, int approximateDelay) {
        return replicationInfo(recordType, status, -1, -1, receivedUpdates, replayedUpdates, approximateDelay);
    }

    private LdapReplicationHealthProbe.ReplicationInfo replicationInfo(
            LdapReplicationHealthProbe.ReplicationRecordType recordType,
            String status,
            int missingChanges,
            int pendingUpdates,
            int receivedUpdates,
            int replayedUpdates,
            int approximateDelay) {
        LdapReplicationHealthProbe.ReplicationInfo info = new LdapReplicationHealthProbe.ReplicationInfo();
        info.recordType = recordType;
        info.status = status;
        info.missingChanges = missingChanges;
        info.pendingUpdates = pendingUpdates;
        info.receivedUpdates = receivedUpdates;
        info.replayedUpdates = replayedUpdates;
        info.currentDelay = approximateDelay;
        return info;
    }

}
