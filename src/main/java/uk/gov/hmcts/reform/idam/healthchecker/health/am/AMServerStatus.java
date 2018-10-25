package uk.gov.hmcts.reform.idam.healthchecker.health.am;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.healthchecker.health.ServerStatus;

import java.io.IOException;

@Slf4j
public class AMServerStatus extends ServerStatus {
    private static final String SERVER_IS_ALIVE = "Server is ALIVE";
    private static final String SERVER_IS_DOWN = "Server is DOWN";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String USER_UNAUTHORIZED = "Unauthorized";
    private static final String SERVER_RETURNED_ACCESS_TOKEN = "Server returned access_token";

    public static Status getStatus(Response response) {
        try {
            final String bodyMessage = getBodyMessage(response);
            if (bodyMessage.contains(SERVER_IS_ALIVE)) {
                return Status.ALIVE;
            } else if (bodyMessage.contains(SERVER_IS_DOWN)) {
                return Status.DOWN;
            }
        } catch (IOException ioEx) {
            log.error("AM server isAlive exception", ioEx);
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
            log.error("AM server token exception", e);
        }

        return Status.SERVER_ERROR;
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
