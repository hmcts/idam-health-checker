package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@CustomLog
@Profile({"tokenstore", "userstore", "ldap"})
public class LdapWorkQueueHealthProbe extends HealthProbe {

    private static final String TAG = "LDAP Work Queue: ";

    private static final String BASE_DN = "cn=work queue,cn=monitor";
    private static final String REQUESTS_IN_QUEUE_ATTRIBUTE = "ds-mon-requests-in-queue";
    private static final String REQUESTS_SUBMITTED_ATTRIBUTE = "ds-mon-requests-submitted";
    private static final String REQUESTS_REJECTED_ATTRIBUTE = "ds-mon-requests-rejected-queue-full";
    private static final String WORK_QUEUE_FILTER = "(objectClass=*)";

    private final LdapTemplate ldapTemplate;
    private final WorkQueueContextMapper workQueueContextMapper;

    public LdapWorkQueueHealthProbe(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
        this.workQueueContextMapper = new WorkQueueContextMapper();
    }

    @Override
    public boolean probe() {
        try {
            LdapQuery workQueueQuery = LdapQueryBuilder.query()
                    .base(BASE_DN)
                    .searchScope(SearchScope.SUBTREE)
                    .attributes(REQUESTS_IN_QUEUE_ATTRIBUTE,
                            REQUESTS_SUBMITTED_ATTRIBUTE,
                            REQUESTS_REJECTED_ATTRIBUTE)
                    .filter(WORK_QUEUE_FILTER);

            List<LdapWorkQueueHealthProbe.WorkQueueInfo> workQueueDataList = ldapTemplate.search(workQueueQuery, workQueueContextMapper);

            if (CollectionUtils.isNotEmpty(workQueueDataList)) {
                log.info(TAG + workQueueDataList.stream().map(LdapWorkQueueHealthProbe.WorkQueueInfo::toString).collect(Collectors.joining("; ")));
                return true;
            }

        } catch (Exception e) {
            log.error(TAG + e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
        }
        return false;
    }


    @EqualsAndHashCode
    static class WorkQueueInfo {
        String dn;
        String requestsInQueue;
        String requestsSubmitted;
        String requestsRejected;

        @Override
        public String toString() {
            return "WorkQueueInfo: {" +
                    "\"dn\": \"" + dn + "\"" +
                    ", \"requestsInQueue\": " + requestsInQueue +
                    ", \"requestsSubmitted\": " + requestsSubmitted +
                    ", \"requestsRejected\": " + requestsRejected +
                    '}';
        }
    }

    static class WorkQueueContextMapper implements ContextMapper<WorkQueueInfo> {

        @Override
        public WorkQueueInfo mapFromContext(Object ctx) throws NamingException {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Name dn = context.getDn();

            log.debug(context.toString());

            String requestsInQueue = context.getStringAttribute(REQUESTS_IN_QUEUE_ATTRIBUTE);
            String requestsSubmitted = context.getStringAttribute(REQUESTS_SUBMITTED_ATTRIBUTE);
            String requestsFailed = context.getStringAttribute(REQUESTS_REJECTED_ATTRIBUTE);

            WorkQueueInfo workQueueInfo = new WorkQueueInfo();
            workQueueInfo.dn = dn.toString();
            workQueueInfo.requestsInQueue = requestsInQueue;
            workQueueInfo.requestsSubmitted = requestsSubmitted;
            workQueueInfo.requestsRejected = requestsFailed;
            return workQueueInfo;
        }
    }

}
