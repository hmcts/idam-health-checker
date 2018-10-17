package com.amido.healthchecker.health.am;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.springframework.http.MediaType;

public interface AMFeignClient {

    @RequestLine("GET /isAlive.jsp")
    Response isAMAlive();

    @RequestLine("POST /oauth2/hmcts/access_token")
    @Headers({"Authorization: Basic {auth}", "Content-Type: " + MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    Response canGenerateAccessToken (
            @Param("auth") String authorization,
            @Param("grant_type") String grantType,
            @Param("username") String username,
            @Param("password") String password,
            @Param("scope") String scope
    );
}
