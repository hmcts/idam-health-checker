package com.amido.healthchecker.health;

import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class ServerStatus {

    protected static String getBodyMessage(Response response) throws IOException {
        log.debug("Response: {}", response);

        final Decoder decoder = new Decoder.Default();
        final String decodedResponse = (String)decoder.decode(response, String.class);
        log.debug("Body: {}", decodedResponse);

        return decodedResponse;
    }
}
