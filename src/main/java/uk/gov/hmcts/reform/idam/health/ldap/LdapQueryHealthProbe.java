package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.List;

@CustomLog
public abstract class LdapQueryHealthProbe<T> extends HealthProbe {

    private final String probeName;
    private final LdapTemplate ldapTemplate;
    private final ContextMapper<T> contextMapper;

    protected LdapQueryHealthProbe(String probeName, LdapTemplate ldapTemplate, ContextMapper<T> contextMapper) {
        this.probeName = probeName;
        this.ldapTemplate = ldapTemplate;
        this.contextMapper = contextMapper;
    }

    public abstract LdapQuery ldapQuery();

    public abstract boolean handleResult(List<T> resultList);

    public boolean handleEmptyResult() {
        return handleError("empty result set from ldap query");
    }

    @Override
    public String getName() {
        return probeName;
    }

    @Override
    public boolean probe() {
        try {
            List<T> resultList = ldapTemplate.search(ldapQuery(), contextMapper);
            if (CollectionUtils.isNotEmpty(resultList)) {
                return handleResult(resultList);
            } else {
                return handleEmptyResult();
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }
}
