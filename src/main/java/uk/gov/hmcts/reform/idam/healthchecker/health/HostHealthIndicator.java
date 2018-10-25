package uk.gov.hmcts.reform.idam.healthchecker.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class HostHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("hostname", getHostName()).build();
    }

    private Object getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Error fetching hostname", e);
            return "--";
        }
    }
}