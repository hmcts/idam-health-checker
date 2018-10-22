package com.amido.healthchecker.health.idm;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Slf4j
@Profile("idm")
public class IDMHealthIndicator implements HealthIndicator {

    private IDMFeignClient idmFeignClient;

    @Autowired
    public IDMHealthIndicator(IDMFeignClient idmFeignClient) {
        this.idmFeignClient = idmFeignClient;
    }

    /**
     * @Should check server is alive
     */
    @Override
    public Health health() {
        return checkIdm();
    }

    /**
     * Specific check.
     * @return Health status
     */
    private Health checkIdm() {
        try {
            final String authorization = Base64.getEncoder().encodeToString(("anonymous:anonymous").getBytes());

            final Response response = idmFeignClient.pingIdm(authorization);

            final IDMServerStatus.Status currentStatus = IDMServerStatus.checkPingResponse(response);

            if (currentStatus.equals(IDMServerStatus.Status.SERVER_READY)) {
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
            log.error("An exception occurred while trying to fetch the IDM ping response", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}