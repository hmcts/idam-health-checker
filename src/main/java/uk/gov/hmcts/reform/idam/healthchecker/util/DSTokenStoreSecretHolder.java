package uk.gov.hmcts.reform.idam.healthchecker.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@Component
public class DSTokenStoreSecretHolder {

    private String passwordName;


    public DSTokenStoreSecretHolder(String passwordName){
        this.passwordName = passwordName;
    }

    public List<String> getDSTokenStoreSecretNames(){
        return Collections.singletonList(passwordName);
    }

}
