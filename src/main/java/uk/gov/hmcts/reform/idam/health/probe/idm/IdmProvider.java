package uk.gov.hmcts.reform.idam.health.probe.idm;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "IdmProvider", url = "${idm.root}")
@Profile("idm")
public interface IdmProvider {

    @GetMapping(
            value = "/info/ping"
    )
    Response ping(@RequestHeader("Authorization") String basicAuth);
}
