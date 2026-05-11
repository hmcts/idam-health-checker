package uk.gov.hmcts.reform.idam.health;

import feign.Client;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients
@Profile({"am","idm","userstore","tokenstore"})
public class FeignConfig {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    @Primary
    public Encoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public CloseableHttpClient feignApacheHttpClient(
            @Value("${feign.httpclient.connection-request-timeout:2000}") int connectionRequestTimeout) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Bean
    public Client feignClient(CloseableHttpClient feignApacheHttpClient) {
        return new ApacheHttpClient(feignApacheHttpClient);
    }
}
