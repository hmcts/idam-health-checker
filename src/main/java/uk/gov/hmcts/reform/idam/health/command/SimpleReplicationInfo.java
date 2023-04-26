package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleReplicationInfo {

    private String instance;
    private String status;
    private String receiveDelay;
    private String replayDelay;
    private String entryCount;

}
