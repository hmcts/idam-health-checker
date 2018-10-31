package uk.gov.hmcts.reform.idam.health.probe;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HealthStatusReport {

    private String statusName;
    private Status status = Status.UNKNOWN;
    private LocalDateTime timestamp;
}
