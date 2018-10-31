package uk.gov.hmcts.reform.idam.health.probe.am;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.util.Base64;

@Component
@Profile("am")
@Slf4j
public class AmPasswordGrantHealthStatus implements HealthStatus {

    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid profile authorities acr roles";

    private final AmProvider amProvider;
    private final String authorization;

    private String host = "localhost";
    private String username = "user";
    private String password = "password";
    private String client = "hmcts";
    private String clientSecret = "password";

    public AmPasswordGrantHealthStatus(AmProvider amProvider) {
        this.amProvider = amProvider;
        this.authorization = "Basic " + Base64.getEncoder().encodeToString((client + ":" + clientSecret).getBytes());
    }

    @Override
    public Status determineStatus() {
        try {
            Response response = amProvider.passwordGrantAccessToken(
                    GRANT_TYPE,
                    host,
                    authorization,
                    username,
                    password,
                    SCOPE);
            if (response.status() == HttpStatus.OK.value()) {
                log.info("password success");
                return Status.UP;
            }
            log.warn("password fail " + response.status());
        } catch (Exception e) {
            log.error("AM password: " + e.getMessage());
        }
        return Status.DOWN;
    }

}
