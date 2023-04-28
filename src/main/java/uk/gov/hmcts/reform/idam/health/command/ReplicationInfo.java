package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReplicationInfo {

    private String context;
    private InstanceType instanceType;
    private String instance;
    private String status;
    private Long receiveDelayMs;
    private Long replayDelayMs;
    private Long entryCount;

}
