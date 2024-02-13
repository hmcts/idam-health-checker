package uk.gov.hmcts.reform.idam.health.idm;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.am.AmProvider;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.Map;
import java.util.Optional;

@Component
@Profile("idm")
@Slf4j
public class IdmCheckRoleExistsHealthProbe extends HealthProbe {

    private final AmProvider amProvider;
    private final IdmProvider idmProvider;
    private final IdmHealthProbeProperties config;

    public IdmCheckRoleExistsHealthProbe(AmProvider amProvider, IdmProvider idmProvider,
                                         IdmHealthProbeProperties config) {
        this.amProvider = amProvider;
        this.idmProvider = idmProvider;
        this.config = config;
    }

    /**
     * @should pass if role exists
     * @should fail if role does not exist
     * @should fail if idm access token is not granted
     * @should fail is am throws an exception for token grant
     */
    @Override
    public boolean probe() {
        try {
            String idmAccessToken = getIdmAccessToken();
            if (idmAccessToken == null) {
                return handleError("failed to get IDM access token from AM");
            }
            Response response = idmProvider.getRole(idmAccessToken, config.getCheckRoleExists().getRoleId());
            if (response.status() == HttpStatus.SC_OK) {
                return handleSuccess();
            } else {
                return handleError("Failed to find role with id '" + config.getCheckRoleExists().getRoleId() + "', response is " + response.status());
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private String getIdmAccessToken() {
        Map<String, String> idmAuth = amProvider.rootPasswordGrantAccessToken(config.getCheckRoleExists().getAmHost(), config.getCheckRoleExists().getAmUser(),
                                                                              config.getCheckRoleExists().getAmPassword(),
                                                                              config.getCheckRoleExists().getIdmClientId(),
                                                                              Optional.ofNullable(config.getCheckRoleExists().getIdmClientSecret()).orElse("default"),
                                                                              config.getCheckRoleExists().getIdmClientScope());
        if (MapUtils.isNotEmpty(idmAuth) && idmAuth.containsKey("access_token")) {
            return "bearer " + idmAuth.get("access_token");
        }
        return null;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
