package com.amido.healthchecker.health;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class AccessTokenHealthIndicator implements HealthIndicator {

    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid profile authorities acr roles";

    @Value("${am.username}")
    private String username;

    @Value("${am.password}")
    private String password;

    @Value("${am.auth.username}")
    private String authUsername;

    @Value("${am.auth.password}")
    private String authPassword;

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
        ServerStatus.Status currentStatus = checkAm();
        return buildHealth(currentStatus);
    }

    /**
     * Specific check.
     * @return
     */
    private ServerStatus.Status checkAm() {
        final String authorization = Base64.getEncoder().encodeToString((authUsername + ":" + authPassword).getBytes());

        final Response response = amFeignClient.canGenerateAccessToken(authorization, GRANT_TYPE, username, password, SCOPE);

        return ServerStatus.checkToken(response);
    }

    private Health buildHealth(ServerStatus.Status currentStatus) {
        Health.Builder healthBuilder;

        if( currentStatus.equals(ServerStatus.Status.ALIVE)) {
            healthBuilder = Health.up();
        } else {
            healthBuilder = Health.down();
        }

        return healthBuilder.withDetail("message", currentStatus.message)
                            .withDetail("errorCode", currentStatus.errorCode)
                            .build();
    }
}