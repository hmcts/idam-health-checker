package uk.gov.hmcts.reform.vault.credential;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.core.JacksonException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenResponseHandler implements ResponseHandler<String> {

    private final static String ACCESS_TOKEN_KEY = "access_token";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String handleResponse(HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }

        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);

        ObjectNode node;
        try {
            node = mapper.readValue(json, ObjectNode.class);
        } catch (JacksonException e) {
            // Keep the checked failure contract expected by the HTTP client and Azure credentials.
            throw new IOException("Invalid JSON in token response", e);
        }
        if (node.has(ACCESS_TOKEN_KEY)) {
            return node.get(ACCESS_TOKEN_KEY).asString();
        }

        throw new HttpResponseException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
            "No access_token parameter present in response");
    }
}
