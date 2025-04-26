package com.example.final_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PurgoClientConfig {

    @Value("${PURGO_CLIENT_API_KEY}")
    private String apiKey;

    @Value("${PURGO_PROXY_BASE_URL}")
    private String baseUrl;

    @Bean
    public RestTemplate purgoRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(baseUrl)
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().set("Authorization", "Bearer " + apiKey);
                    return execution.execute(request, body);
                })
                .build();
    }
}
