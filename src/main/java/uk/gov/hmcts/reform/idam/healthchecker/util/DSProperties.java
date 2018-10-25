package uk.gov.hmcts.reform.idam.healthchecker.util;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DSProperties {

    private String baseUrl;
    private String userDN;
    private String password;
    private String base;


}
