package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/*
 * 配置类，用于创建AliOssUtil对象，因为要通过配置属性类注入属性
 * */
@Component
@Slf4j
public class OssConfiguration {

    // @Bean - SpringMVC初始化时会执行
    @Bean
    // @ConditionalOnMissingBean - 只对没有执行过的进行操作，避免重复执行
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("正在创建阿里云文件上传工具类");
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
