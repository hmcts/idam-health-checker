package com.amido.healthchecker.health;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Slf4j
public class AccessTokenHealthIndicator implements HealthIndicator {

    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid profile authorities acr roles";

    @Value("${am.client.name}")
    private String clientName;

    private AMFeignClient amFeignClient;

    @Autowired
    public AccessTokenHealthIndicator(AMFeignClient amFeignClient) {
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
            final String authorization = Base64.getEncoder().encodeToString((clientName + ":" + System.getProperty("AM_PASSWORD")).getBytes());

            final Response response = amFeignClient.canGenerateAccessToken(authorization, GRANT_TYPE,
                    System.getProperty("SMOKE_TEST_USER_USERNAME"),
                    System.getProperty("SMOKE_TEST_USER_PASSWORD"), SCOPE);

            final ServerStatus.Status currentStatus = ServerStatus.checkToken(response);

            if (currentStatus.equals(ServerStatus.Status.RETURNED_ACCESS_TOKEN)) {
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