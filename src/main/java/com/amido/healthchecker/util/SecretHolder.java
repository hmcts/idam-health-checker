package com.amido.healthchecker.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
public class SecretHolder {
    public static final String CONST_LDAP_PASSWORD = "Pa55word11";
    private static final String SECRET_NAME_AM_PASSWORD = "am-password";
    private static final String SECRET_NAME_SMOKE_TEST_USER_USERNAME = "smoke-test-user-username";
    private static final String SECRET_NAME_SMOKE_TEST_USER_PASSWORD = "smoke-test-user-password";
    private static final String SECRET_NAME_CTS_LDAP_PASSWORD = "cts-ldap-password";
    private static final String SECRET_NAME_CRS_LDAP_PASSWORD = "crs-ldap-password";

    private List<String> secretNames = Arrays.asList(SECRET_NAME_AM_PASSWORD,
            SECRET_NAME_SMOKE_TEST_USER_USERNAME,
            SECRET_NAME_SMOKE_TEST_USER_PASSWORD,
            SECRET_NAME_CRS_LDAP_PASSWORD,
            SECRET_NAME_CTS_LDAP_PASSWORD);

    private Map<String, Object> secretsMap;

    public SecretHolder(){
        secretsMap = new HashMap<>();
    }

    public String getAmPassword() {
        return String.valueOf(secretsMap.get(SECRET_NAME_AM_PASSWORD));
    }

    public String getSmokeTestUserUsername() {
        return String.valueOf(secretsMap.get(SECRET_NAME_SMOKE_TEST_USER_USERNAME));
    }

    public String getSmokeTestUserPassword() {
        return String.valueOf(secretsMap.get(SECRET_NAME_SMOKE_TEST_USER_PASSWORD));
    }

    public String getCtsLdapPassword() {
        return String.valueOf(secretsMap.get(SECRET_NAME_CTS_LDAP_PASSWORD));
    }

    public String getCrsLdapPassword() {
        return String.valueOf(secretsMap.get(SECRET_NAME_CRS_LDAP_PASSWORD));
    }

    public void setSecretsMap(String key, Object value) {
        this.secretsMap.put(key, value);
    }

    public String toString() {
        return secretsMap.toString();
    }

}