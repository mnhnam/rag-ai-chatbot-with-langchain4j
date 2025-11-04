package chatbot.chatbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import reactor.core.publisher.FluxSink;

/**
 * A handler that bridges LangChain4j's streaming chat responses
 * with a Reactor FluxSink to emit data reactively (e.g., for SSE or WebFlux).
 */
public class StreamChatHandler implements StreamingChatResponseHandler {

    // FluxSink used to push partial responses to the client
    private FluxSink<String> sink;

    // Shared ObjectMapper instance (thread-safe and reused for performance)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Compact record class to represent a streaming response as a JSON object
    private record ResponsePayload(String response, boolean done) {}

    /**
     * Init new StreamChatHandler and binds a FluxSink to this handler
     * so that it can send data to the client.
     * 
     * @param sink the sink used to stream data reactively
     */
    public StreamChatHandler (FluxSink<String> sink) {
        this.sink = sink;
    }

    /**
     * Called when a partial (token-by-token) response is received from the LLM.
     * This method wraps the token in a JSON object and send it via SSE.
     * 
     * @param partialResponse the current token or partial content
     */
    @Override
    public void onPartialResponse(String partialResponse) {
        
        // Stream the partial response to client
        if (sink != null) {
            // Convert the partial response into a JSON string and push to the sink
            sink.next(serializeResponse(partialResponse, false));
        }
    }

    /**
     * Called when the full response has been received.
     * This method sends the complete response as the final message,
     * appending "<<Finish>>" to indicate the end of the stream.
     * 
     * @param completeResponse the complete response
     */
    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {

        // Send the complete response and signal completion
        if (sink != null) {
            sink.next(serializeResponse(completeResponse.aiMessage().text(), true));
            sink.complete();
        }
    }

    /**
     * Called when an error occurs during streaming.
     * This will propagate the error to the client, ending the stream.
     *
     * @param error the encountered exception or issue
     */
    @Override
    public void onError(Throwable error) {
        // Signal an error occurred
        if (sink != null) {
            sink.error(error);
        }
    }

    /**
     * Utility method to serialize a partial response into a JSON string using Jackson.
     * This ensures that special characters and whitespace are preserved.
     *
     * @param partialResponse the partial response to send
     * @param isDone mark that the response is completed
     * 
     * @return a JSON-formatted string, e.g., {"token":"Hello ", "done":false}
     */
    private static String serializeResponse(String partialResponse, boolean isDone) {
        try {
            return objectMapper.writeValueAsString(new ResponsePayload(partialResponse, isDone));
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}