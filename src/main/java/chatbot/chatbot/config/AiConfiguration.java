package chatbot.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Configuration
public class AiConfiguration {
    
    @Value("${app.ai.server-url}")
    private String serverUrl;
    
    @Value("${app.ai.chat-model-name}")
    private String chatModelName;
    
    @Value("${app.ai.embedding-model-name}")
    private String embeddingModelName;

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
    public OllamaStreamingChatModel chatModel() {
        return OllamaStreamingChatModel.builder()
            .baseUrl(serverUrl)
            .modelName(chatModelName)
            .build();
    }

    @Bean
    public DimensionAwareEmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
            .baseUrl(serverUrl)
            .modelName(embeddingModelName)
            .build();
    }

    @Bean
    public PgVectorEmbeddingStore embeddingStore(DimensionAwareEmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.builder()
            .host(dbHost)
            .port(dbPort)
            .user(dbUser)
            .password(dbPassword)
            .database(dbName)
            .createTable(false)
            .table(tableName)
            .dimension(embeddingModel.dimension())
            .build();
    }
}