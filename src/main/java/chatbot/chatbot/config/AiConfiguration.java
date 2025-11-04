package chatbot.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Configuration
public class AiConfiguration {
    
    @Value("${app.ai.server-url}")
    private String serverUrl;
    
    @Value("${app.ai.chat-model-name}")
    private String chatModelName;
    
    @Value("${app.ai.embedding-model-name}")
    private String embeddingModelName;

    @Value("${app.ai.api-key}")
    private String apiKey; 

    @Value("${app.database.host}")
    private String dbHost;
    
    @Value("${app.database.port}")
    private int dbPort;
    
    @Value("${app.database.name}")
    private String dbName;
    
    @Value("${app.database.user}")
    private String dbUser;
    
    @Value("${app.database.password}")
    private String dbPassword;
    
    @Value("${app.database.table}")
    private String tableName;
    
    @Value("${app.vectorstore.min-score}")
    private double minScore;
    
    @Value("${app.vectorstore.raw-data-dir}")
    private String rawDataDir;

    @Bean
    public GoogleAiGeminiStreamingChatModel chatModel() {
        return GoogleAiGeminiStreamingChatModel.builder()
            .modelName(chatModelName)
            .apiKey(apiKey)
            .build();
    }

    @Bean
    public GoogleAiEmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
            .modelName(embeddingModelName)
            .apiKey(apiKey)
            .build();
    }

    @Bean
    public PgVectorEmbeddingStore embeddingStore(GoogleAiEmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.builder()
            .host(dbHost)
            .port(dbPort)
            .user(dbUser)
            .password(dbPassword)
            .database(dbName)
            .createTable(true)
            .table(tableName)
            .dimension(embeddingModel.dimension())
            .build();
    }
}