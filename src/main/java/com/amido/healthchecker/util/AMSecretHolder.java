package com.amido.healthchecker.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@Component
public class AMSecretHolder {

    private String amPasswordName;

    private String smokeTestUserUsername;

    private String smokeTestUserPassword;


    public AMSecretHolder(String amPasswordName, String smokeTestUserUsername, String smokeTestUserPassword){
        this.amPasswordName = amPasswordName;
        this.smokeTestUserUsername = smokeTestUserUsername;
        this.smokeTestUserPassword = smokeTestUserPassword;
    }


    public List<String> getAMSecretNames(){
        return Arrays.asList(amPasswordName, smokeTestUserUsername, smokeTestUserPassword);
    }

}
