package uk.gov.hmcts.reform.idam.healthchecker.health.am;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.healthchecker.util.SecretHolder;

import java.util.Base64;

@Component
@Slf4j
@Profile("am")
public class AMAccessTokenHealthIndicator implements HealthIndicator {

    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid profile authorities acr roles";

    @Value("${am.client.name}")
    private String clientName;

    @Value("${am.smoke.test.user.username}")
    private String smokeTestUsername;

    private AMFeignClient amFeignClient;

    private SecretHolder secretHolder;

    @Autowired
    public AMAccessTokenHealthIndicator(AMFeignClient amFeignClient, SecretHolder secretHolder) {
        this.amFeignClient = amFeignClient;
        this.secretHolder = secretHolder;
    }

    /**
     * @Should check server is alive
     */
    @Override
    public Health health() {
        return checkAm();
    }

    /**
     * Specific check.
     * @return Health status
     */
    private Health checkAm() {
        try {
            final Response response = amFeignClient.canGenerateAccessToken(getAuthorization(), GRANT_TYPE,
                    smokeTestUsername,
                    secretHolder.getSmokeTestUserPassword(), SCOPE);


            final AMServerStatus.Status currentStatus = AMServerStatus.checkToken(response);
            if (currentStatus.equals(AMServerStatus.Status.RETURNED_ACCESS_TOKEN)) {
                return Health.up()
                        .withDetail("message", currentStatus.message)
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", currentStatus.message)
                        .withDetail("errorCode", currentStatus.errorCode)
                        .build();
            }
        } catch (Exception e) {
            log.error("An exception occurred while trying to fetch AM server access_token", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }

    private String getAuthorization() {
        return Base64.getEncoder().encodeToString((clientName + ":" + secretHolder.getAmPassword()).getBytes());
    }
}