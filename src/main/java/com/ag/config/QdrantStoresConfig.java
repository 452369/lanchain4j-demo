package com.ag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class QdrantStoresConfig {

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    @Bean(name = "programmingQdrantStore")
    @Primary
    public SimpleQdrantStore programmingQdrantStore(
            @Value("${qdrant.programming.collection-name}") String collection) {
        return new SimpleQdrantStore(host, port, collection);
    }

    /**
     * 法律助手
     * @param collection
     * @return
     */
    @Bean(name = "lawQdrantStore")
    public SimpleQdrantStore lawQdrantStore(
            @Value("${qdrant.law.collection-name}") String collection) {
        return new SimpleQdrantStore(host, port, collection);
    }
}