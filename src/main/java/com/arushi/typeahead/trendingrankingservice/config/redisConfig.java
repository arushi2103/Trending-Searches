package com.arushi.typeahead.trendingrankingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class redisConfig {
    // Configuration for Redis connection
    // This class can be used to set up RedisTemplate, RedisConnectionFactory, etc.
    // For now, we will keep it empty as we are using default configurations.
    // You can add your Redis configuration here if needed.
    @Bean
    public RedisTemplate<String, List<String>> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String,List<String>> template= new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //Set String Serializer for keys and values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
