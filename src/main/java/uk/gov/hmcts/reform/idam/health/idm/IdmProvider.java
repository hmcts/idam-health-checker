package uk.gov.hmcts.reform.idam.health.idm;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "IdmProvider", url = "${idm.root}")
@Profile("idm")
public interface IdmProvider {

    @GetMapping("/info/ping")
    Map<String, String> ping(@RequestHeader("Authorization") String basicAuth);

    @GetMapping("/config/provisioner.openicf/ldap?_fields={fields}")
    Map<String, Object> checkLdap(@RequestHeader("Authorization") String basicAuth,
                                  @RequestParam(value = "fields", required = true) String fields);
}
