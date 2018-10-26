package uk.gov.hmcts.reform.idam.healthchecker.health.ds;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@Profile("ds-replication")
@Slf4j
public class DSReplicationHealthIndicator implements HealthIndicator {

    @Value("${ds.replication.check.script}")
    private String script;

    @Override
    public Health health() {
        final int errorCode = executeCommand(script);
        log.info("Replication check returned {}", errorCode);

        if (errorCode != 0) {
            return Health.down()
                    .withDetail("error", errorCode)
                    .build();
        }

        return Health.up().build();
    }

    protected int executeCommand(final String script) {
        final StringBuffer output = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(script);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            final String result = output.toString();
            return Integer.parseInt(result);

        } catch (Exception e) {
            log.error("Replication check exception", e);
            return 1;
        }
    }
}
