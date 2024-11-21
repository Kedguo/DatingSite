package com.y.datingsite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 配置类用于设置Spring Data Redis的RedisTemplate。
 * 主要配置连接工厂以及键和值的序列化方式。
 * @ClassName: RedisTemplateConfig
 * @apiNote 该类确保RedisTemplate被正确配置，以便在Spring应用中更高效地使用Redis进行数据存储和访问。
 */
@Configuration
public class RedisTemplateConfig {

    /**
     * 配置并返回RedisTemplate实例，用于Redis操作的主要接口。
     * @param connectionFactory 由Spring注入的Redis连接工厂，用于创建Redis连接。
     * @return 配置好的RedisTemplate实例。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        // 创建RedisTemplate对象，指定键类型为String，值类型为Object
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置Redis连接工厂，这是Redis操作的核心依赖
        redisTemplate.setConnectionFactory(connectionFactory);
        // 使用String序列化器对Key进行序列化，以保证键的存储格式为字符串
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 使用GenericJackson2JsonRedisSerializer序列化器对值进行JSON序列化
        // 这样可以确保Value以JSON格式存储，便于阅读和兼容性
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }

}