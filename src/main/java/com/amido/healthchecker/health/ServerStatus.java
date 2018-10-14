package com.amido.healthchecker.health;

import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ServerStatus {
    private static final String SERVER_IS_ALIVE = "Server is ALIVE";
    private static final String SERVER_IS_DOWN = "Server is DOWN";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String USER_UNAUTHORIZED = "Unauthorized";
    private static final String SERVER_RETURNED_ACCESS_TOKEN = "Server returned access_token";

    final static Logger logger = LoggerFactory.getLogger(ServerStatus.class);

    public static Status getStatus(Response response) {
        try {
            String bodyMessage = getBodyMessage(response);

            if (bodyMessage.contains(SERVER_IS_ALIVE)) return Status.ALIVE;
            if (bodyMessage.contains(SERVER_IS_DOWN)) return Status.DOWN;
        } catch (IOException ioEx){
            logger.error("Couldn't get isAlive status", ioEx);
        }
        return Status.SERVER_ERROR;
    }

    public static Status checkToken(Response response) {
        try {
            final String bodyMessage = getBodyMessage(response);
            if (response.status() == HttpStatus.OK.value() && bodyMessage.contains("access_token")) {
                return Status.RETURNED_ACCESS_TOKEN;
            } else if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
                return Status.UNAUTHORIZED;
            } else if (response.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return Status.SERVER_ERROR;
            } else {
                return Status.DOWN;
            }
        } catch (IOException e) {
            logger.error("Couldn't check token", e);
        }

        return Status.SERVER_ERROR;
    }

    private static String getBodyMessage(Response response) throws IOException {
        logger.info("Response: " + response);

        String bodyMessage = new BufferedReader(new InputStreamReader(response.body().asInputStream())).lines().collect(Collectors.joining("\n"));
        logger.info("Body: " + bodyMessage);

        return bodyMessage;
    }

    enum Status {
        ALIVE(0, SERVER_IS_ALIVE),
        RETURNED_ACCESS_TOKEN(0, SERVER_RETURNED_ACCESS_TOKEN),
        DOWN(503, SERVER_IS_DOWN),
        SERVER_ERROR(500, INTERNAL_SERVER_ERROR),
        UNAUTHORIZED(401, USER_UNAUTHORIZED);

        int errorCode;
        String message;

        Status(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }

}
