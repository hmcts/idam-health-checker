package uk.gov.hmcts.reform.idam.healthchecker.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Component
public class SecretHolder {
    private List<String> secretNames;

    private Map<String, Object> secretsMap;

    private AMSecretHolder amSecretHolder;
    private DSUserStoreSecretHolder dsUserStoreSecretHolder;
    private DSTokenStoreSecretHolder dsTokenStoreSecretHolder;

    @Autowired
    public SecretHolder(AMSecretHolder amSecretHolder,
                        DSTokenStoreSecretHolder dsTokenStoreSecretHolder,
                        DSUserStoreSecretHolder dsUserStoreSecretHolder) {
        this.amSecretHolder = amSecretHolder;
        this.dsTokenStoreSecretHolder = dsTokenStoreSecretHolder;
        this.dsUserStoreSecretHolder = dsUserStoreSecretHolder;

        this.secretsMap = new HashMap<>();
        this.secretNames = Stream.of(amSecretHolder.getAMSecretNames(),
                dsTokenStoreSecretHolder.getDSTokenStoreSecretNames(),
                dsUserStoreSecretHolder.getDSUserStoreSecretNames())
                .flatMap(Collection :: stream)
                .collect(Collectors.toList());
    }

    public String getAmPassword() {
        return String.valueOf(secretsMap.get(amSecretHolder.getAmPasswordName()));
    }

    public String getSmokeTestUserPassword() {
        return String.valueOf(secretsMap.get(amSecretHolder.getSmokeTestUserPassword()));
    }

    public String getDSTokenStorePassword() {
        return String.valueOf(secretsMap.get(dsTokenStoreSecretHolder.getPasswordName()));
    }

    public String getDSUserStorePassword() {
        return String.valueOf(secretsMap.get(dsUserStoreSecretHolder.getPasswordName()));
    }

    public void setSecretsMap(String key, Object value) {
        this.secretsMap.put(key, value);
    }

    public String toString() {
        return secretsMap.toString();
    }

}
