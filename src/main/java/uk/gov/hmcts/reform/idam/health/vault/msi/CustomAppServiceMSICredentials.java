package uk.gov.hmcts.reform.idam.health.vault.msi;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;

import java.io.IOException;

public class CustomAppServiceMSICredentials extends AzureTokenCredentials {

    private static final String MSI_TOKEN_URL = "http://169.254.169.254/metadata/identity/oauth2/token";
    private static final String MSI_QUERY_STRING = "?api-version=2018-02-01&resource=https%3A%2F%2Fvault.azure.net";
    private static final String MSI_KEYVAULT_TOKEN_URL = MSI_TOKEN_URL + MSI_QUERY_STRING;
    private static final String REQUEST_HEADER_METADATA = "Metadata";
    private static final String REQUEST_HEADER_METADATA_VALUE_TRUE = "true";

    private final HttpRequestFactory httpRequestFactory;
    private final JsonFactory jsonFactory;


    public CustomAppServiceMSICredentials(AzureEnvironment environment) {
        super(environment, null);

        this.httpRequestFactory = new NetHttpTransport().createRequestFactory();
        this.jsonFactory = new JacksonFactory();
    }

    protected CustomAppServiceMSICredentials(AzureEnvironment environment,
                                             HttpRequestFactory httpRequestFactory, JsonFactory jsonFactory) {
        super(environment, null);

        this.httpRequestFactory = httpRequestFactory;
        this.jsonFactory = jsonFactory;
    }


    @Override
    public String getToken(String resource) throws IOException {

        HttpRequest tokenRequest = buildTokenHttpRequest();
        AccessTokenRespHolder accessTokenRespHolder = tokenRequest.execute().parseAs(AccessTokenRespHolder.class);

        return accessTokenRespHolder.getAccessToken();
    }

    protected HttpRequest buildTokenHttpRequest() throws IOException {
        GenericUrl genericUrl = new GenericUrl(MSI_KEYVAULT_TOKEN_URL);

        HttpHeaders headers = new HttpHeaders();
        headers.set(REQUEST_HEADER_METADATA, REQUEST_HEADER_METADATA_VALUE_TRUE);
        HttpRequest tokenRequest  = httpRequestFactory.buildGetRequest(genericUrl)
                .setHeaders(headers).setParser(new JsonObjectParser(jsonFactory));
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
