package uk.gov.hmcts.reform.idam.healthchecker.health.idm;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.healthchecker.health.ServerStatus;

import java.io.IOException;

@Slf4j
public class IDMServerStatus extends ServerStatus {
    protected static final String SERVER_IS_READY = "OpenIDM ready";
    protected static final String SERVER_NOT_READY = "OpenIDM server is not ready";

    public static Status checkPingResponse(Response response) {
        try {
            final String bodyMessage = getBodyMessage(response);
            if (response.status() == HttpStatus.OK.value() && bodyMessage.contains(SERVER_IS_READY)) {
                return Status.SERVER_READY;
            }
        } catch (IOException ioEx) {
            log.error("Couldn't get isAlive status", ioEx);
        }

        return Status.SERVER_ERROR;
    }

    enum Status {
        SERVER_READY(0, SERVER_IS_READY),
        SERVER_ERROR(503, SERVER_NOT_READY);

        int errorCode;
        String message;

        Status(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }

}
