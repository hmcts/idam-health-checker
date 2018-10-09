package com.amido.healthchecker.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AmHealthIndicator implements HealthIndicator {

    public AmHealthIndicator() {

    }

    /**
     *
     */
    @Override
    public Health health() {
        int errorCode = checkAm(); // Perform some specific health check
        if (errorCode != 0) {
            return Health.down()
                    .withDetail("Error Code", errorCode)
                    .build();
        }
        return Health.up()
                .withDetail("Some message", "42")
                .build();
    }

    /**
     * Specific check.
     * @return
     */
    public int checkAm() {
        // Specific logic to check health here
        return 0;
    }
}