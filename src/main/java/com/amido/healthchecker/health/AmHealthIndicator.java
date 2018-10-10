package com.amido.healthchecker.health;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
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
        ServerStatus.Status currentStatus = checkAm();
        return buildHealth(currentStatus);
    }

    /**
     * Specific check.
     * @return
     */
    private ServerStatus.Status checkAm(){
        Response response = amFeignClient.isAMAlive();
        return ServerStatus.getStatus(response);
    }

    private Health buildHealth(ServerStatus.Status currentStatus) {
        Health.Builder healthBuilder;

        if(currentStatus.equals(ServerStatus.Status.ALIVE)){
            healthBuilder = Health.up();
        } else {
            healthBuilder = Health.down();
        }

        return healthBuilder.withDetail("message", currentStatus.message)
                            .withDetail("errorCode", currentStatus.errorCode)
                            .build();

    }
}