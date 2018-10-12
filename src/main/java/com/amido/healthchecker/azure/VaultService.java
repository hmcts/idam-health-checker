package com.amido.healthchecker.azure;

public interface VaultService {

    void loadSecret(final String systemPropertyName, final String secretName);
}
