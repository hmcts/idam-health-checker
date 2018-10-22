package com.amido.healthchecker.health.am;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static com.amido.healthchecker.HealthcheckerApplication.AM_PASSWORD;
import static com.amido.healthchecker.HealthcheckerApplication.SMOKE_TEST_USER_PASSWORD;
import static com.amido.healthchecker.HealthcheckerApplication.SMOKE_TEST_USER_USERNAME;

@Component
@Slf4j
@Profile("am")
public class AMAccessTokenHealthIndicator implements HealthIndicator {

    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid profile authorities acr roles";

    @Value("${am.client.name}")
    private String clientName;

    private AMFeignClient amFeignClient;

    @Autowired
    public AMAccessTokenHealthIndicator(AMFeignClient amFeignClient) {
        this.amFeignClient = amFeignClient;
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
            final String authorization = Base64.getEncoder().encodeToString((clientName + ":" + System.getProperty(AM_PASSWORD)).getBytes());

            final Response response = amFeignClient.canGenerateAccessToken(authorization, GRANT_TYPE,
                    System.getProperty(SMOKE_TEST_USER_USERNAME),
                    System.getProperty(SMOKE_TEST_USER_PASSWORD), SCOPE);

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
}