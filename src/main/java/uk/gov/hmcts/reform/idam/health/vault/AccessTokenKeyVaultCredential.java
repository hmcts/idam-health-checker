package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class AccessTokenKeyVaultCredential extends AzureTokenCredentials {

    private final static String METADATA_HEADER = "Metadata";

    private final String tokenEndpoint;

    public AccessTokenKeyVaultCredential(String tokenEndpoint) {
        super(AzureEnvironment.AZURE, null);
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public String getToken(String resource) throws IOException {
        HttpClient client = HttpClientBuilder.create().disableAutomaticRetries().build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(tokenEndpoint)
                .setHeader(METADATA_HEADER, Boolean.TRUE.toString())
                .build();

        return client.execute(request, TokenResponseHandler.getInstance());
    }
}
