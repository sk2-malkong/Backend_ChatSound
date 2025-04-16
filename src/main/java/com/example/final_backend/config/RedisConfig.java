package com.example.final_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis와 Spring Boot를 연동하기 위한 설정
 * - RedisTemplate을 커스터마이징해 Redis에 저장되는 key/value의 직렬화 방식 지정
 * - RedisConnectionFactory를 통해 Redis와 연결 후 RedisTemplate 객체 생성
 * - JWT 토큰 관리는 설정된 RedisTemplate으로 접근
 */

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // (선택) key와 value를 직렬화 방식 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}
