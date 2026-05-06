package com.ag.config;
import com.ag.service.AiCodeService;
import com.ag.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 用工厂的方式创建AiCodeService
 */
@Configuration
public class AiCodeServiceFactory {
    @Resource
    private OpenAiChatModel myOpenAiChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private OpenAiStreamingChatModel openAiStreamingChatModel;
    @Bean
    public AiCodeService aiCodeService(){
        //会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(30);
        //创建AiCodeService
        AiCodeService aiCodeService = AiServices.builder(AiCodeService.class)
                .chatModel(myOpenAiChatModel)
                .streamingChatModel(openAiStreamingChatModel)//流式输出模型
                .chatMemory(chatMemory) //会话记忆
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) //每个会话独立存储
                .contentRetriever(contentRetriever) //内容检索器
                .tools(new InterviewQuestionTool())
                .toolProvider(mcpToolProvider) //mcp 工具调用
                .build();
        return aiCodeService;
    }
}
