package uk.gov.hmcts.reform.idam.health.probe;

public interface HealthProbe {

    boolean probe();

    default String getName() {
        return this.getClass().getSimpleName();
    };
}
