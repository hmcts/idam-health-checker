package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "TokenStoreProvider", url = "${tokenstore.root}")
@Profile("tokenstore")
public interface TokenStoreProvider {

    @GetMapping("/alive")
    void alive();

    @GetMapping("/healthy")
    void healthy();

}
