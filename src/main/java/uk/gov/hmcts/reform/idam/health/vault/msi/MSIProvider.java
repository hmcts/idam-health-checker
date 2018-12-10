package uk.gov.hmcts.reform.idam.health.vault.msi;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="msiProvider", url="http://169.254.169.254/metadata")
public interface MSIProvider {

    @GetMapping(value = "/identity/oauth2/token?api-version=2018-02-01&resource=https%3A%2F%2Fvault.azure.net")
    AccessTokenRespHolder getMSIAccessToken(@RequestHeader("Metadata") String metadata);


}
