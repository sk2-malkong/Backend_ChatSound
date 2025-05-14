package com.example.final_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 *프록시 서버와 JWT 인증이 필요한 외부 요청을 자동 처리해주는 HTTP 클라이언트 설정 클래스
 * - RestTemplate Bean을 등록해 프록시 API와 HTTP 통신을 가능하게 함.
 * - JWT 토큰을 자동으로 헤더에 포함하여 인증된 요청을 보낼 수 있도록 설정함.
 * - RestTemplate 사용 시 매 요청마다 Authorization 헤더를 삽입해 보안 처리 자동화 역할 수행.
 */

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
