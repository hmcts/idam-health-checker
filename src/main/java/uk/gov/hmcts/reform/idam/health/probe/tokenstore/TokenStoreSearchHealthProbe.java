package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import javax.naming.directory.SearchControls;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("tokenstore")
@Slf4j
public class TokenStoreSearchHealthProbe extends BaseHealthProbe {

    private static final String LDAP_SEARCH_IN_REPLICATION = "cn=Replication,cn=monitor";
    private static final String LDAP_SEARCH_ANY_OBJECT = "(objectClass=*)";
    private static final String LDAP_CN_ATTRIBUTE = "cn";

    private final TokenStoreHealthProbeProperties healthProbeProperties;
    private final LdapTemplate ldapTemplate;

    private HealthStatusReport latestReport;

    public TokenStoreSearchHealthProbe(
            LdapTemplate ldapTemplate,
            TokenStoreHealthProbeProperties healthProbeProperties) {
        this.ldapTemplate = ldapTemplate;
        this.healthProbeProperties = healthProbeProperties;

        this.latestReport = new HealthStatusReport();
        this.latestReport.setStatus(Status.UNKNOWN);
    }

    @Override
    public HealthStatusReport getLatestReport() {
        return latestReport;
    }

    @Override
    @Scheduled(fixedDelayString = "#{@tokenStoreHealthProbeProperties.search.checkInterval}")
    public void refresh() {
        try {
            List<Object> searchResponse = ldapTemplate.search(
                    LDAP_SEARCH_IN_REPLICATION,
                    LDAP_SEARCH_ANY_OBJECT,
                    SearchControls.SUBTREE_SCOPE,
                    (AttributesMapper<Object>) attrs -> attrs.get(LDAP_CN_ATTRIBUTE).get());
            latestReport.setStatus(searchResponse.isEmpty() ? Status.DOWN : Status.UP);
        } catch (Exception e) {
            log.error("TOKENSTORE: " + e.getMessage());
            latestReport.setStatus(Status.DOWN);
        }
        latestReport.setTimestamp(LocalDateTime.now());
    }

    @Override
    public Long getExpiryInterval() {
        return healthProbeProperties.getSearch().getFreshnessInterval();
    }
}
