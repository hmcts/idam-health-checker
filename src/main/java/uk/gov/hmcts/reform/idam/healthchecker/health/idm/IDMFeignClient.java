package uk.gov.hmcts.reform.idam.healthchecker.health.idm;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

public interface IDMFeignClient {

    @RequestLine("GET /info/ping")
    @Headers({"Authorization: Basic {auth}"})
    Response pingIdm(@Param("auth") String authorization);
}
