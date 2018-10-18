package com.amido.healthchecker.health.idm;

import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServerStatus {
    protected static final String SERVER_IS_READY = "OpenIDM ready";
    protected static final String SERVER_NOT_READY = "OpenIDM NOT ready";

    public static Status checkPingResponse(Response response) {
        try {
            final String bodyMessage = getBodyMessage(response);
            if (bodyMessage.contains(SERVER_IS_READY)) {
                return Status.SERVER_READY;
            }
        } catch (IOException ioEx) {
            log.error("Couldn't get isAlive status", ioEx);
        }

        return Status.SERVER_ERROR;
    }

    private static String getBodyMessage(Response response) throws IOException {
        log.info("Response: " + response);

        final Decoder decoder = new Decoder.Default();
        final String decodedResponse = (String)decoder.decode(response, String.class);
        log.info("Body: " + decodedResponse);

        return decodedResponse;
    }

    enum Status {
        SERVER_READY(0, SERVER_IS_READY),
        SERVER_ERROR(500, SERVER_NOT_READY);

        int errorCode;
        String message;

        Status(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }

}
