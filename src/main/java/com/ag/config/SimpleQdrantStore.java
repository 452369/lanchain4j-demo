package com.ag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.*;
import okhttp3.*;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 兼容 LangChain4j 1.1.0 的 Qdrant 存储实现
 */
public class SimpleQdrantStore implements EmbeddingStore<TextSegment> {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final String collectionName;

    public SimpleQdrantStore(String host, int port, String collectionName) {
        this.baseUrl = "http://" + host + ":" + port;
        this.collectionName = collectionName;
    }

    @PostConstruct
    public void init() {
        System.out.println("[Qdrant] 已连接到 " + baseUrl + "/" + collectionName);
    }

    // ---------- 实现必需的核心方法 ----------
    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        addInternal(id, embedding, null);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        addInternal(id, embedding, null);
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        String id = UUID.randomUUID().toString();
        addInternal(id, embedding, textSegment);
        return id;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        return embeddings.stream().map(this::add).collect(Collectors.toList());
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        if (embeddings.size() != textSegments.size()) {
            throw new IllegalArgumentException("embeddings 和 textSegments 数量不一致");
        }
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            ids.add(add(embeddings.get(i), textSegments.get(i)));
        }
        return ids;
    }

    /**
     * 新版接口要求的 search 方法
     */
    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("vector", request.queryEmbedding().vectorAsList());
            body.put("limit", request.maxResults());
            body.put("with_payload", true);
            body.put("score_threshold", request.minScore());

            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName + "/points/search")
                    .post(RequestBody.create(mapper.writeValueAsString(body), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                String json = response.body().string();
                Map<?, ?> map = mapper.readValue(json, Map.class);
                List<Map<?, ?>> points = (List<Map<?, ?>>) map.get("result");
                if (points == null) {
                    return new EmbeddingSearchResult<>(Collections.emptyList());
                }

                List<EmbeddingMatch<TextSegment>> matches = points.stream()
                        .map(p -> toMatch(p, request.queryEmbedding()))
                        .collect(Collectors.toList());

                return new EmbeddingSearchResult<>(matches);
            }
        } catch (IOException e) {
            throw new RuntimeException("Qdrant 搜索失败", e);
        }
    }

    /**
     * 如果接口中仍然存在 findRelevant 方法（可能是默认实现或已弃用），
     * 这里提供显式实现以便直接调用。如果接口中已不存在此方法，可以删除。
     */
    public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(referenceEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        return search(request).matches();
    }

    // ---------- 私有辅助方法 ----------
    private void addInternal(String id, Embedding embedding, TextSegment segment) {
        try {
            Map<String, Object> point = new HashMap<>();
            point.put("id", id);
            point.put("vector", embedding.vectorAsList());

            Map<String, Object> payload = new HashMap<>();
            if (segment != null) {
                payload.put("text", segment.text());
                payload.putAll(segment.metadata().toMap());
            }
            point.put("payload", payload);

            Map<String, Object> body = new HashMap<>();
            body.put("points", Collections.singletonList(point));

            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName + "/points")
                    .put(RequestBody.create(mapper.writeValueAsString(body), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Qdrant 存储失败: " + response.code());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("添加向量到 Qdrant 失败", e);
        }
    }

    private EmbeddingMatch<TextSegment> toMatch(Map<?, ?> point, Embedding queryEmbedding) {
        double score = ((Number) point.get("score")).doubleValue();
        String id = point.get("id").toString();
        Map<String, Object> payload = (Map<String, Object>) point.get("payload");
        String text = (String) payload.get("text");
        payload.remove("text");
        Metadata metadata = Metadata.from(payload.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
        TextSegment segment = TextSegment.from(text, metadata);
        // Qdrant 返回的点中不包含向量，这里传入 queryEmbedding 作为占位
        return new EmbeddingMatch<>(score, id, queryEmbedding, segment);
    }
}