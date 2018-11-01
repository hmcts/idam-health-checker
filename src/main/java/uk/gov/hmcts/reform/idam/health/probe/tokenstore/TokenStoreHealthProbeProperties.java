package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("tokenstore.healthprobe")
@Getter
@Setter
public class TokenStoreHealthProbeProperties {

    @Getter
    @Setter
    static class Search {
        private Long freshnessInterval;
        private Long checkInterval;
    }

    private Search search;
}