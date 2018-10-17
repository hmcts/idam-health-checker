package com.amido.healthchecker.azure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DummyVaultService implements VaultService {

    @Autowired
    private Environment env;

    public void loadSecret(final String systemPropertyName, final String secretName) {
        final String value = env.getProperty(secretName);
        if (value != null) {
            System.setProperty(systemPropertyName, value);
        } else {
            throw new IllegalStateException("Couldn't find secret " + secretName);
        }
    }
}