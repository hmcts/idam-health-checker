package uk.gov.hmcts.reform.idam.health.probe;

import java.util.Base64;

public abstract class RestHealthProbe<T> extends BaseHealthProbe {

    public RestHealthProbe(Long expiryTimeMillis) {
        super(expiryTimeMillis);
    }

    protected void refresh() {
        try {
            T content = makeRestCall();
            if ((content != null) && (validateContent(content))) {
                setStatus(Status.UP);
                return;
            }
        } catch (Exception e) {
            handleException(e);
        }
        setStatus(Status.DOWN);
    }

    protected String encode(String identity, String secret) {
        return Base64.getEncoder().encodeToString((identity + ":" + secret).getBytes());
    }

    protected abstract T makeRestCall();

    protected abstract boolean validateContent(T content);

    protected abstract void handleException(Exception e);
}
