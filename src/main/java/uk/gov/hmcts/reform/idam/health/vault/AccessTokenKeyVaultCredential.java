package uk.gov.hmcts.reform.idam.health.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccessTokenKeyVaultCredential extends AzureTokenCredentials {

    private static final String MSI_URL = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https%3A%2F%2Fvault.azure.net";

    private final static String METADATA_HEADER = "Metadata";
    private final static String ACCESS_TOKEN_KEY = "access_token";

    private final String tokenEndpoint;

    public AccessTokenKeyVaultCredential() {
        this(MSI_URL);
    }

    public AccessTokenKeyVaultCredential(String tokenEndpoint) {
        super(AzureEnvironment.AZURE, null);
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public String getToken(String resource) throws IOException {
        HttpClient client = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler()).build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(tokenEndpoint)
                .setHeader(METADATA_HEADER, Boolean.TRUE.toString())
                .build();
        return client.execute(request, new TokenResponseHandler());
    }

    protected class TokenResponseHandler implements ResponseHandler<String> {

        ObjectMapper mapper = new ObjectMapper();

        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            ObjectNode node = mapper.readValue(json, ObjectNode.class);
            if (node.has(ACCESS_TOKEN_KEY)) {
                return mapper.writeValueAsString(node.get(ACCESS_TOKEN_KEY));
            }

            // TODO should probably throw an exception
            return null;
        }
    }

}
