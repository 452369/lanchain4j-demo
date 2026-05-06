package com.ag.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LawRagConfig {

    @Resource(name = "lawQdrantStore")
    private EmbeddingStore<TextSegment> lawStore;

    @Resource
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Bean(name = "lawContentRetriever")
    public ContentRetriever lawContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(lawStore)
                .embeddingModel(openAiEmbeddingModel)
                .maxResults(5)
                .minScore(0.7)
                .build();
    }
}