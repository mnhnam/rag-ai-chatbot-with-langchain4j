package chatbot.chatbot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import chatbot.chatbot.handler.StreamChatHandler;
import chatbot.chatbot.service.AiService;
import chatbot.chatbot.service.VectorStoreService;
import chatbot.chatbot.utils.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@RestController
@RequestMapping(value = "/api")
public class ChatController {

    // Store active conversations using a UUID as the key and message as the value.
    private Map<String, String> conversations = new HashMap<>();
    
    private final AiService aiService;
    private final VectorStoreService vectorStoreService;

    public ChatController(AiService aiService, VectorStoreService vectorStoreService) {
        this.aiService = aiService;
        this.vectorStoreService = vectorStoreService;
    }

    
    /**
     * Accepts a user message via POST, starts the streaming generation,
     * and returns a conversationId for SSE subscription.
     */
    @PostMapping("/chat")
    public ResponseEntity<String> simpleChat(@RequestBody String message) {

        // Generate a unique ID for this conversation
        String conversationId = UUID.randomUUID().toString();

        // Store the message so it can be accessed during the streaming phase
        conversations.put(conversationId, message);

         // Return the conversation ID to the client to start listening for stream response
        return ResponseEntity.ok(conversationId);
    }

    /**
     * Handles Server-Sent Events (SSE) using the conversationId.
     * This endpoint allows the frontend to stream the partial responses.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String conversationId) {

        // Create a Flux that will push data to the client over SSE
        return Flux.create((FluxSink<String> sink) -> {
            
            // Retrieve the message associated with the conversation
            String message = conversations.get(conversationId);

            
            // Search the vector store with the user message
            List<String> relevantContexts = vectorStoreService.search(message, 3);

            // Log the relevant contexts found
            //System.out.println("Relevant contexts found: " + relevantContexts.size());
            //for (String context : relevantContexts) {
                //System.out.println("Context: " + context);
            //}

            // Create a handler to receive partial responses from streaming AI
            StreamChatHandler handler = new StreamChatHandler(sink);

            // Start streaming the AI response
            aiService.streamRag(message, relevantContexts, handler);

            // Remove the conversaion from the conversations map
            conversations.remove(conversationId);

        }, FluxSink.OverflowStrategy.BUFFER); // Use buffering strategy to avoid backpressure issues
    }

    /**
     * Handles file upload for vector store documents.
     * Accepts multiple files and stores them in the raw_data folder.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();

        // Create raw_data directory if it doesn't exist
        try {
            FileUtils.createRawDataDirectoryIfNotExists();
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Failed to create raw_data directory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Validate file
        errors = FileUtils.validateMultipartFile(file);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Save file
        if (!FileUtils.saveMultipartFile(file, errors)) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Prepare response
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Gets list of uploaded files in the raw_data directory
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> getUploadedFiles() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> fileInfos = FileUtils.getFileInfos();

        if (fileInfos.isEmpty()) {
            response.put("success", false);
            response.put("message", "No files found in raw_data directory.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

         // Prepare response
        response.put("success", true);
        response.put("files", fileInfos);
        response.put("count", fileInfos.size());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/deleteFile")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam String fileName) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();

        // Delete file
        if (!FileUtils.deleteFile(fileName, errors)) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Prepare response
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Creates/initializes the vector store index and processes documents
     */
    @PostMapping("/createIndex")
    public ResponseEntity<Map<String, Object>> createVectorStoreIndex() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Process documents in the raw_data directory
            if (!vectorStoreService.processDocuments()) {
                response.put("success", false);
                response.put("error", "Failed to process documents");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            response.put("success", true);
            response.put("message", "Vector store index created and documents processed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Exception occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Resets/deletes the vector store index
     */
    @PostMapping("/resetIndex")
    public ResponseEntity<Map<String, Object>> resetIndex() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Reset the vector store index
            if (!vectorStoreService.resetIndex()) {
                response.put("success", false);
                response.put("error", "Failed to reset vector store index");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            response.put("success", true);
            response.put("message", "Vector store index reset successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Exception occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}