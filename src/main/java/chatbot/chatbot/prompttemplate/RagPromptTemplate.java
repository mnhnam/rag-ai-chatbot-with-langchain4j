package chatbot.chatbot.prompttemplate;

public final class RagPromptTemplate {
    public static final String RAG_SYSTEM_PROMPT_TEMPLATE = """        
        You are a helpful and factual AI assistant. 
        Use only the information provided in the retrieved context to answer the question. 
        If the answer cannot be found in the context, say "I don’t have enough information to answer that."

        Follow these rules:
        - Be concise, clear, and accurate.
        - Do not fabricate or assume facts.
        - Cite or refer to sources if available in the context.
    """;

    public static final String RAG_USER_PROMPT_TEMPLATE = """
        Question:
        {{question}}

        Context (retrieved documents):
        {{context}}

        Instructions:
        1. Read the question carefully.
        2. Review all the provided context snippets.
        3. Provide the best possible answer using only the given information.
        4. If the context does not contain the answer, respond with: 
        "I don’t have enough information to answer that."
    """;
}
