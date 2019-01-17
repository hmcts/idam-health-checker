package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReplicationInfo {

    private String suffix;
    private String hostName;
    private String entries;
    private String replicationEnabled;
    private String dsID;
    private String rsId;
    private String rsPort;
    private String missingChanges;
    private String ageOfMissingChanges;
    private String securityEnabled;

}
