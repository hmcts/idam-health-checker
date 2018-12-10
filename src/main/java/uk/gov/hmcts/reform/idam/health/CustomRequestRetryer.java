package uk.gov.hmcts.reform.idam.health;

import feign.RetryableException;
import feign.Retryer;

public class CustomRequestRetryer implements Retryer {

    private final int maxAttempts;
    private final long backoff;
    private int attempt;

    public CustomRequestRetryer(){
        this(3, 2000);
    }

    public CustomRequestRetryer(int maxAttempts, long backoff) {
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            throw e;
        }

        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new CustomRequestRetryer(maxAttempts, backoff);
    }
}
