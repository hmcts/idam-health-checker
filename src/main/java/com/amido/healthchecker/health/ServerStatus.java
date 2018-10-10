package com.amido.healthchecker.health;


import feign.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ServerStatus {


    private static final String SERVER_IS_ALIVE = "Server is ALIVE";
    private static final String SERVER_IS_DOWN = "Server is DOWN";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";


    public static Status getStatus(Response response) {
        try {
            String bodyMessage = new BufferedReader(new InputStreamReader(response.body().asInputStream())).lines().collect(Collectors.joining("\n"));

            if (bodyMessage.contains(SERVER_IS_ALIVE)) return Status.ALIVE;
            if (bodyMessage.contains(SERVER_IS_DOWN)) return Status.DOWN;
        } catch (IOException ioEx){
            //TODO log message parsing error
        }
        return Status.SERVER_ERROR;
    }

    enum Status {
        ALIVE(0, SERVER_IS_ALIVE),
        DOWN(503, SERVER_IS_DOWN),
        SERVER_ERROR(500, INTERNAL_SERVER_ERROR);

        int errorCode;
        String message;

        Status(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

    }



}
