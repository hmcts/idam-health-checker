package uk.gov.hmcts.reform.idam.health;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeignConfigTest {

    @Test
    public void feignApacheHttpClientSetsConnectionRequestTimeout() throws Exception {
        try (CloseableHttpClient httpClient = new FeignConfig().feignApacheHttpClient(1234)) {
            RequestConfig requestConfig = ((Configurable) httpClient).getConfig();

            assertThat(requestConfig.getConnectionRequestTimeout(), is(1234));
        }
    }
}
