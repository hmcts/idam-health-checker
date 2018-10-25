package uk.gov.hmcts.reform.idam.healthchecker.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@Component
public class DSUserStoreSecretHolder {

    private String passwordName;


    public DSUserStoreSecretHolder(String passwordName){
        this.passwordName = passwordName;
    }

    public List<String> getDSUserStoreSecretNames(){
        return Collections.singletonList(passwordName);
    }

}
