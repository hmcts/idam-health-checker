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
public class LdapWorkQueueHealthProbeTest {

    @Mock
    private LdapTemplate ldapTemplate;

    private LdapWorkQueueHealthProbe probe;

    @BeforeEach
    public void setup() {
        probe = new LdapWorkQueueHealthProbe("probeName", ldapTemplate);
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
