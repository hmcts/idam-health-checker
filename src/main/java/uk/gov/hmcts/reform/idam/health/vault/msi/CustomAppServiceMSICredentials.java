package uk.gov.hmcts.reform.idam.health.vault.msi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;

import java.io.IOException;

public class CustomAppServiceMSICredentials extends AzureTokenCredentials {

    private static final String MSI_TOKEN_URL = "http://169.254.169.254/metadata/identity/oauth2/token";
    private static final String MSI_QUERY_STRING = "?api-version=2018-02-01&resource=https%3A%2F%2Fvault.azure.net";
    private static final String MSI_KEYVAULT_TOKEN_URL = MSI_TOKEN_URL.concat(MSI_QUERY_STRING);
    private static final String REQUEST_HEADER_METADATA = "Metadata";
    private static final String REQUEST_HEADER_METADATA_VALUE_TRUE = "true";


    public CustomAppServiceMSICredentials(AzureEnvironment environment) {
        super(environment, null);
    }

    @Override
    public String getToken(String resource) throws IOException {

        HttpRequest tokenRequest = buildTokenHttpRequest();
        String rawResponse = tokenRequest.execute().parseAsString();
        AccessTokenRespHolder accessTokenRespHolder = new ObjectMapper()
                                                            .readValue(rawResponse, AccessTokenRespHolder.class);

        return accessTokenRespHolder.getAccessToken();
    }

    private HttpRequest buildTokenHttpRequest() throws IOException {
        HttpRequestFactory httpRequestFactory = new NetHttpTransport().createRequestFactory();
        GenericUrl genericUrl = new GenericUrl(MSI_KEYVAULT_TOKEN_URL);

        HttpHeaders headers = new HttpHeaders();
        headers.set(REQUEST_HEADER_METADATA, REQUEST_HEADER_METADATA_VALUE_TRUE);
        HttpRequest tokenRequest  = httpRequestFactory.buildGetRequest(genericUrl).setHeaders(headers);
        tokenRequest.setUnsuccessfulResponseHandler(
            new HttpBackOffUnsuccessfulResponseHandler(getExponentialBackOff())).setNumberOfRetries(3);

        return tokenRequest;
    }

    private ExponentialBackOff getExponentialBackOff() {
        return new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(500)
                .setMaxElapsedTimeMillis(90000)
                .setMaxIntervalMillis(6000)
                .setMultiplier(1.5)
                .setRandomizationFactor(0.5)
                .build();
    }
}
