package uk.gov.hmcts.reform.idam.health.probe;

public class FixedHealthProbeIndicator implements HealthProbeIndicator {

    private final boolean fixedValue;

    public FixedHealthProbeIndicator(boolean fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public boolean isOkay() {
        return fixedValue;
    }

}
