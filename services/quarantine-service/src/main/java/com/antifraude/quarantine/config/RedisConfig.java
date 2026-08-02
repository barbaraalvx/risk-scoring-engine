package com.antifraude.quarantine.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.antifraude.quarantine.readmodel.QuarantineView;

/**
 * Configuração do read model de quarentena em Redis (CQRS).
 */
@Configuration
public class RedisConfig {

    /**
     * Cria o {@link RedisTemplate} usado para ler e escrever o read model
     * de quarentena, serializando os valores em JSON.
     *
     * @param connectionFactory Fábrica de conexões Redis.
     * @param objectMapper      ObjectMapper configurado pelo Spring Boot (com suporte a java.time).
     * @return Template configurado para {@link QuarantineView}.
     */
    @Bean
    public RedisTemplate<String, QuarantineView> quarantineViewRedisTemplate(
            final RedisConnectionFactory connectionFactory,
            final ObjectMapper objectMapper) {

        final Jackson2JsonRedisSerializer<QuarantineView> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, QuarantineView.class);

        final RedisTemplate<String, QuarantineView> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }
}
