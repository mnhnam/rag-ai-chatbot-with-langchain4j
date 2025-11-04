package chatbot.chatbot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import chatbot.chatbot.handler.StreamChatHandler;
import chatbot.chatbot.prompttemplate.RagPromptTemplate;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

@Service
public class AiService {

    private final GoogleAiGeminiStreamingChatModel chatModel;
    private final GoogleAiEmbeddingModel embeddingModel;

    public AiService(GoogleAiGeminiStreamingChatModel chatModel, GoogleAiEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public GoogleAiEmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * Sends a message to the OpenAI chat model with the given contexts and streams the response using the given handler.
     * 
     * @param message the user message
     * @param contexts the relevant contexts to include in the prompt
     * @param handler the streaming response handler
     */
    public void streamRag(String message, List<String> contexts, StreamChatHandler handler) {
        List<ChatMessage> messages = new ArrayList<>();

        // Build system message
        SystemMessage systemMessage = new SystemMessage(RagPromptTemplate.RAG_SYSTEM_PROMPT_TEMPLATE);

        // Build user message with question and contexts
        UserMessage userMessage = new UserMessage(
            RagPromptTemplate.RAG_USER_PROMPT_TEMPLATE
                .replace("{{question}}", message)
                .replace("{{context}}", String.join("\n\n", contexts))
        );

        // Add messages to the list
        messages.add(systemMessage);
        messages.add(userMessage);

        // Stream chat response
        chatModel.chat(messages, handler);
    }
}