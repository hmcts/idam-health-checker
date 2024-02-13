package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LdapWorkQueueHealthProbe extends LdapQueryHealthProbe<LdapWorkQueueHealthProbe.WorkQueueInfo> {

    private static final String BASE_DN = "cn=work queue,cn=monitor";
    private static final String WORK_QUEUE_FILTER = "(objectClass=*)";

    private static final String REQUESTS_IN_QUEUE_ATTRIBUTE = "ds-mon-requests-in-queue";
    private static final String REQUESTS_SUBMITTED_ATTRIBUTE = "ds-mon-requests-submitted";
    private static final String REQUESTS_REJECTED_ATTRIBUTE = "ds-mon-requests-rejected-queue-full";

    public LdapWorkQueueHealthProbe(String probeName, LdapTemplate ldapTemplate) {
        super(probeName, ldapTemplate, new WorkQueueContextMapper());
    }

    @Override
    public LdapQuery ldapQuery() {
        return LdapQueryBuilder.query()
                .base(BASE_DN)
                .searchScope(SearchScope.SUBTREE)
                .attributes(REQUESTS_IN_QUEUE_ATTRIBUTE,
                        REQUESTS_SUBMITTED_ATTRIBUTE,
                        REQUESTS_REJECTED_ATTRIBUTE)
                .filter(WORK_QUEUE_FILTER);
    }

    @Override
    public boolean handleResult(List<LdapWorkQueueHealthProbe.WorkQueueInfo> resultList) {
        log.info("{}: {}", getName(), resultList.stream().map(LdapWorkQueueHealthProbe.WorkQueueInfo::toString).collect(Collectors.joining("; ")));
        return true;
    }

    @Override
    public Logger getLogger() {
        return log;
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
