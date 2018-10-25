package com.amido.healthchecker.health.am;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("am")
public class AMHealthIndicator implements HealthIndicator {

    private AMFeignClient amFeignClient;

    @Autowired
    public AMHealthIndicator(AMFeignClient amFeignClient) {
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

            final AMServerStatus.Status currentStatus = AMServerStatus.getStatus(response);
            if (currentStatus.equals(AMServerStatus.Status.ALIVE)) {
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
            log.error("AM server ping exception", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}