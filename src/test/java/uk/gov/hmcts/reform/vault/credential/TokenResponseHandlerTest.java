package uk.gov.hmcts.reform.vault.credential;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenResponseHandlerTest {
    private final TokenResponseHandler handler = new TokenResponseHandler();

    @Test
    void returnsAccessToken() throws IOException {
        assertEquals("token+&=", handler.handleResponse(response(200, "OK",
                "{\"access_token\":\"token+&=\",\"expires_in\":3600}")));
    }

    @Test
    void rejectsMissingAccessToken() {
        var error = assertThrows(HttpResponseException.class,
                () -> handler.handleResponse(response(200, "OK", "{\"expires_in\":3600}")));
        assertEquals(500, error.getStatusCode());
        assertEquals("No access_token parameter present in response", error.getReasonPhrase());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "not-json", "[]", ""})
    void rejectsInvalidJsonWithCheckedException(String body) {
        assertThrows(IOException.class, () -> handler.handleResponse(response(200, "OK", body)));
    }

    @Test
    void preservesHttpErrorBeforeParsingBody() {
        var error = assertThrows(HttpResponseException.class,
                () -> handler.handleResponse(response(401, "Unauthorized", "not-json")));
        assertEquals(401, error.getStatusCode());
        assertEquals("Unauthorized", error.getReasonPhrase());
    }

    private static BasicHttpResponse response(int status, String reason, String body) {
        var response = new BasicHttpResponse(HttpVersion.HTTP_1_1, status, reason);
        response.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return response;
    }
}
