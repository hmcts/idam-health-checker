package uk.gov.hmcts.reform.idam.health.am;

import com.google.common.collect.ImmutableMap;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "AmProvider", url = "${am.root}")
@Profile({"am", "idm"})
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
            value = "/json/health/live"
    )
    Response healthLive();

    @GetMapping(
            value = "/json/health/ready"
    )
    Response healthReady();

    @PostMapping(
            value = "/oauth2/realms/hmcts/access_token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Map<String, String> accessToken(@RequestHeader("Authorization") String auth,
                         @RequestHeader("Host") String host,
                         Map<String, ?> formParams);

    default Map<String, String> rootPasswordGrantAccessToken(
            String host,
            String username,
            String password,
            String clientId,
            String clientSecret,
            String scope
    ) {
        return rootAccessToken(
                host,
                ImmutableMap.<String, String>builder()
                        .put("grant_type", "password")
                        .put("username", username)
                        .put("password", password)
                        .put("client_id", clientId)
                        .put("client_secret", clientSecret)
                        .put("scope", scope)
                        .build()
        );
    }

    @PostMapping(
            value = "/oauth2/access_token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Map<String, String> rootAccessToken(@RequestHeader("Host") String host,
                                    Map<String, ?> formParams);

}
