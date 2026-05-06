package com.ag.model;


import com.ag.listener.ChatModelListenerConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 自定义的ai聊天模型配置类
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.openai.chat-model")
@Data
public class AiChatModelConfig {
    private String modelName;
    private String apiKey;
    private String baseUrl;

    @Resource
    private ChatModelListenerConfig chatModelListenerConfig;

    @Bean
    public ChatModel myOpenAiChatModel(){
        return OpenAiChatModel.builder()
                .modelName(modelName)
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .temperature(0.6)
                .maxTokens(2000)
                .listeners(List.of(chatModelListenerConfig))
                .build();
    }
}
