package uk.gov.hmcts.reform.idam.health.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LdapWorkQueueHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    private LdapWorkQueueHealthProbe probe;

    @Before
    public void setup() {
        probe = new LdapWorkQueueHealthProbe(ldapTemplate);
    }

    @Test
    public void testProbe_successOneResult() {
        List<LdapWorkQueueHealthProbe.WorkQueueInfo> searchResult = new ArrayList<>();
        searchResult.add(workQueueInfo("100", "submitted-details", "rejected-details"));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapWorkQueueHealthProbe.WorkQueueContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_successTwoResults() {
        List<LdapWorkQueueHealthProbe.WorkQueueInfo> searchResult = new ArrayList<>();
        searchResult.add(workQueueInfo("100", "submitted-details", "rejected-details"));
        searchResult.add(workQueueInfo("100", "submitted-details", "rejected-details"));

        when(ldapTemplate.search(any(LdapQuery.class), any(LdapWorkQueueHealthProbe.WorkQueueContextMapper.class))).thenReturn(searchResult);
        assertThat(probe.probe(), is(true));
    }

    @Test
    public void testProbe_failNoResults() {
        when(ldapTemplate.search(any(LdapQuery.class), any(LdapWorkQueueHealthProbe.WorkQueueContextMapper.class))).thenReturn(Collections.emptyList());
        assertThat(probe.probe(), is(false));
    }

    @Test
    public void testProbe_failWithException() {
        when(ldapTemplate.search(any(LdapQuery.class), any(LdapWorkQueueHealthProbe.WorkQueueContextMapper.class))).thenThrow(new RuntimeException());
        assertThat(probe.probe(), is(false));
    }

    private LdapWorkQueueHealthProbe.WorkQueueInfo workQueueInfo(String queueCount, String submittedDetails, String rejectedDetails) {
        LdapWorkQueueHealthProbe.WorkQueueInfo workQueueInfo = new LdapWorkQueueHealthProbe.WorkQueueInfo();
        workQueueInfo.requestsInQueue = queueCount;
        workQueueInfo.requestsSubmitted = submittedDetails;
        workQueueInfo.requestsRejected = rejectedDetails;
        workQueueInfo.dn = "dn";
        return workQueueInfo;
    }

}
