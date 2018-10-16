package com.amido.healthchecker.health;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AmHealthIndicator implements HealthIndicator {

    private AMFeignClient amFeignClient;

    @Autowired
    public AmHealthIndicator(AMFeignClient amFeignClient) {
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
            final Response response = amFeignClient.isAMAlive();

            final ServerStatus.Status currentStatus = ServerStatus.getStatus(response);
            if (currentStatus.equals(ServerStatus.Status.ALIVE)) {
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
            log.error("An exception occurred while checking if the AM server is alive", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}