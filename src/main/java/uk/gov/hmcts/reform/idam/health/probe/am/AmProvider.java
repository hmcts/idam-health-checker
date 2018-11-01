package uk.gov.hmcts.reform.idam.health.probe.am;

import com.google.common.collect.ImmutableMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "AmProvider", url = "${am.root}")
@Profile("am")
public interface AmProvider {

    default Map<String, String> passwordGrantAccessToken(
            String grantType,
            String host,
            String auth,
            String username,
            String password,
            String scope
    ) {
        return accessToken(
                auth,
                host,
                ImmutableMap.of(
                        "grant_type", grantType,
                        "username", username,
                        "password", password,
                        "scope", scope
                )
        );
    }

    @GetMapping(
            value = "/isAlive.jsp"
    )
    String isAlive();

    @PostMapping(
            value = "/oauth2/access_token?realm=hmcts",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Map<String, String> accessToken(@RequestHeader("Authorization") String auth,
                         @RequestHeader("Host") String host,
                         Map<String, ?> formParams);

}
