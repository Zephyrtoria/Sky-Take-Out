package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        log.info("开始创建redis模版对象...");
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        // 设置 Redis 连接工厂对象（工厂对象自动被注入到容器中）
        stringRedisTemplate.setConnectionFactory(factory);
        // 设置redis key的序列化器
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        return stringRedisTemplate;
    }
}
