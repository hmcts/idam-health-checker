package uk.gov.hmcts.reform.idam.health.probe.idm;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.util.Base64;

@Component
@Profile("idm")
@Slf4j
public class IdmPingHealthStatus implements HealthStatus {

    private static String ANONYMOUS_USER = "anonymous";
    private static String ANONYMOUS_PASSWORD = "anonymous";

    private final IdmProvider idmProvider;
    private final String authorization;

    public IdmPingHealthStatus(IdmProvider idmProvider) {
        this.idmProvider = idmProvider;
        this.authorization = "Basic " + Base64.getEncoder().encodeToString((ANONYMOUS_USER + ":" + ANONYMOUS_PASSWORD).getBytes());
    }

    @Override
    public Status determineStatus() {
        try {
            Response pingResponse = idmProvider.ping(authorization);
            if (pingResponse.status() == HttpStatus.OK.value()) {
                return Status.UP;
            }
        } catch (Exception e) {
            log.error("IDM: " + e.getMessage());
        }
        return Status.DOWN;
    }
}
