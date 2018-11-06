package uk.gov.hmcts.reform.idam.health.probe;

public class FixedHealthProbeExecutor implements HealthProbeExecutor {

    private final boolean fixedValue;

    public FixedHealthProbeExecutor(boolean fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public boolean isOkay() {
        return fixedValue;
    }

}
