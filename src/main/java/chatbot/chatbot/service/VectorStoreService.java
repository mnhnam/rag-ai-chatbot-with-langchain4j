package chatbot.chatbot.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import chatbot.chatbot.textsplitter.SimpleTextSplitter;
import chatbot.chatbot.utils.FileUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Service
public class VectorStoreService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DimensionAwareEmbeddingModel embeddingModel;

    public VectorStoreService(EmbeddingStore<TextSegment> embeddingStore, DimensionAwareEmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Process and index text documents from the raw_data directory
     * This is a simplified version that manually processes text files
     */
    public boolean processDocuments() {
        if (embeddingStore == null) {
            System.err.println("Cannot process documents: EmbeddingStore not initialized");
            return false;
        }
        
        try {
            Path rawDataDir = Paths.get("raw_data");
            if (!Files.exists(rawDataDir)) {
                System.out.println("Raw data directory does not exist");
                return false;
            }

            // Get all text and markdown files
            try (Stream<Path> paths = Files.walk(rawDataDir)) {
                List<Path> documentPaths = paths
                    .filter(Files::isRegularFile)
                    .filter(FileUtils::isAllowedFileType)
                    .toList();

                if (documentPaths.isEmpty()) {
                    System.out.println("No documents found to process");
                    return true;
                }

                System.out.println("Processing " + documentPaths.size() + " documents...");

                // Process each document
                for (Path docPath : documentPaths) {
                    processDocument(docPath);
                }

                System.out.println("Successfully processed all documents");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error processing documents: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Process a single document: load, split into chunks, and store as text segments
     * This is a simplified version that manually splits text
     */
    private void processDocument(Path docPath) throws IOException {
        // Read the entire file content
        String content = Files.readString(docPath);
        
        // Simple text splitting
        List<TextSegment> segments = SimpleTextSplitter.splitTextIntoSegments(content, docPath.toString());
        
        // Store segments with embeddings
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }
        
        System.out.println("Processed document: " + docPath.getFileName() + " (" + segments.size() + " segments)");
    }

    /**
     * Search for similar content in the vector store
     * Note: This requires actual embeddings to work properly
     */
    public List<String> search(String query, int maxResults) {
        if (embeddingStore == null) {
            System.err.println("Cannot search: EmbeddingStore not initialized");
            return List.of();
        }
        
        try {
            // Get embedding for the query message
            var queryEmbedding = embeddingModel.embed(query).content();

            // Build search request
            var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(0.7) // Only return results with similarity > 0.7
                .build();
            
            var searchResult = embeddingStore.search(searchRequest);
            
            // Extract text from matches
            return searchResult.matches().stream()
                .map(match -> match.embedded().text())
                .toList();
                
        } catch (Exception e) {
            System.err.println("Error searching vector store: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Reset the vector store index by removing all entries
     * @return
     */
    public boolean resetIndex() {
        try {
            embeddingStore.removeAll();
            System.out.println("Successfully reset index");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to reset index: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test database connectivity
     */
    public boolean testConnection() {
        try {
            if (embeddingStore == null) {
                System.err.println("EmbeddingStore is null - not initialized");
                return false;
            }
            
            // Try a simple operation to test connectivity
            var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed("test").content())
                .maxResults(1)
                .build();
            
            embeddingStore.search(searchRequest);
            System.out.println("Database connection test successful");
            return true;
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get embedding store instance for use in other services
     */
    public EmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }
}