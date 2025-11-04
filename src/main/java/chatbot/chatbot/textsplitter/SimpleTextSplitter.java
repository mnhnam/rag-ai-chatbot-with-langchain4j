package chatbot.chatbot.textsplitter;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.segment.TextSegment;

public final class SimpleTextSplitter {
    /**
     * Simple text splitting into segments
     */
    public static List<TextSegment> splitTextIntoSegments(String text, String documentName) {
        List<TextSegment> segments = new ArrayList<>();
        int chunkSize = 500;
        int overlap = 100;

        for (int i = 0; i < text.length(); i += chunkSize - overlap) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end);
            
            // Create metadata with source information
            var metadata = new dev.langchain4j.data.document.Metadata();
            metadata.put("source", documentName);
            metadata.put("chunk_index", String.valueOf(segments.size()));
            segments.add(TextSegment.from(chunk, metadata));

            if (end >= text.length()) break;
        }
        
        return segments;
    }
}
