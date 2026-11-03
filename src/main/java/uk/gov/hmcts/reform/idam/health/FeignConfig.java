package uk.gov.hmcts.reform.idam.health;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients
@Profile({"am","idm","userstore","tokenstore"})
public class FeignConfig {
}
