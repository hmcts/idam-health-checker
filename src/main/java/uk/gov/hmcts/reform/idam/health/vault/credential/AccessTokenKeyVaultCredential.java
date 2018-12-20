package uk.gov.hmcts.reform.idam.health.vault.credential;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import uk.gov.hmcts.reform.idam.health.vault.TokenResponseHandler;

import java.io.IOException;

public class AccessTokenKeyVaultCredential extends AzureTokenCredentials {

    private final static String METADATA_HEADER = "Metadata";

    private final String tokenEndpoint;

    private final int maxRetries;

    private final int retryInterval;

    public AccessTokenKeyVaultCredential(String tokenEndpoint, int  maxRetries, int retryInterval) {
        super(AzureEnvironment.AZURE, null);
        this.tokenEndpoint = tokenEndpoint;
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public String getToken(String resource) throws IOException {
        HttpClient client = HttpClientBuilder.create().setServiceUnavailableRetryStrategy(
                new ServiceUnavailableRetryStrategy() {
                    @Override
                    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        return statusCode == 500 && executionCount < maxRetries;
                    }

                    @Override
                    public long getRetryInterval() {
                        return retryInterval;
                    }
                }).build();

        HttpUriRequest request = RequestBuilder.get()
                .setUri(tokenEndpoint)
                .setHeader(METADATA_HEADER, Boolean.TRUE.toString())
                .build();

        return client.execute(request, TokenResponseHandler.getInstance());
    }
}
