package chatbot.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Configuration
public class AiConfiguration {
    
    @Value("${app.ai.server-url}")
    private String serverUrl;
    
    @Value("${app.ai.chat-model-name}")
    private String chatModelName;
    
    @Value("${app.ai.embedding-model-name}")
    private String embeddingModelName;

    @Value("#{new Integer(${app.ai.embedding-dimension})}")
    private Integer dimension;

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
    public OpenAiStreamingChatModel chatModel() {
        return OpenAiStreamingChatModel.builder()
            .modelName(chatModelName)
            .apiKey(apiKey)
            .build();
    }

    @Bean
    public OpenAiEmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
            .modelName(embeddingModelName)
            .dimensions(dimension)
            .apiKey(apiKey)
            .build();
    }

    @Bean
    public PgVectorEmbeddingStore embeddingStore(OpenAiEmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.builder()
            .host(dbHost)
            .port(dbPort)
            .user(dbUser)
            .password(dbPassword)
            .database(dbName)
            .createTable(true)
            .table(tableName)
            .dimension(dimension)
            .build();
    }
}