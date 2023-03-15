package uk.gov.hmcts.reform.idam.health.userstore;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "UserStoreProvider", url = "${userstore.root}")
@Profile("userstore")
public interface UserStoreProvider {

    @GetMapping("/alive")
    void alive();

    @GetMapping("/healthy")
    void healthy();

}
