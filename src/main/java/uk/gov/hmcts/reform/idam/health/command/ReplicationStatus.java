package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReplicationStatus {

    private List<String> errors = new ArrayList<>();
    private Map<String, List<ReplicationInfo>> contextReplicationInfo = new HashMap<>();

}
