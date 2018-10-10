package com.amido.healthchecker.health;


import feign.RequestLine;
import feign.Response;

public interface AMFeignClient {

    @RequestLine("GET /isAlive.jsp")
    Response isAMAlive();

}
