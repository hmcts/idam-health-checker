package uk.gov.hmcts.reform.idam.health.probe;

import javax.annotation.Nullable;

public interface HealthProbeIndicator {

    boolean isOkay();

    @Nullable
    String getDetails();

    String getProbeName();
}
