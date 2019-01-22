package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReplicationStatus {

    private List<String> errors = new ArrayList<>();
    private ReplicationInfo hostReplicationInfo;
    private List<ReplicationInfo> replicationInfoList = new ArrayList<>();

}
