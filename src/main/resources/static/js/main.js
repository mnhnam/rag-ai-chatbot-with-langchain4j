// Bind event for the textbox: press Enter key to send message
$(document).ready(function() {
    var textbox = $('#user-input').get(0);
    textbox.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
            return;
          }
    });
});

// Sends the user's message to the server and handles the streamed response
async function sendMessage() {
    const inputBox = $('#user-input');
    const text = inputBox.val().trim();
    inputBox.val('');

    if (!text) return;

    // Render user message in UI
    renderUserMessage(text);

    // Send a POST request to the backend with the user message
    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(text)
        });

        // Await and retrieve the conversation ID from the server response
        const conversationId = await response.text();

        // Start streaming the AI's response using SSE
        stream(conversationId);
    } catch (error) {
        // Handle any error during message sending or response retrieval
        console.error('Failed to send or receive message:', error);
        displayErrorMessage('Unable to contact server. Please check your connection and try again.');
    }
}

// Function to stream AI response using Server-Sent Events (SSE)
function stream(conversationId) {
    // Initially render an empty AI message to be updated progressively
    renderAIMessage('');

    // Open SSE connection to backend with conversation ID
    eventSource = new EventSource(`/api/stream?conversationId=${conversationId}`);

    // Listen for incoming SSE messages
    eventSource.onmessage = (event) => {
        try {
            // Parse the SSE message data (expected to be in JSON format)
            const data = JSON.parse(event.data);

            if (data.response) {
                // Append or replace the AI message content in the UI
                updateAIMessage(data.response, data.done);
            }

            if (data.done) {
                // Close the SSE connection once the message is fully received
                eventSource.close();
            }
        } catch (e) {
            console.error('Error parsing stream message:', e);
        }
    };

    // Handle SSE errors (e.g. connection drops)
    eventSource.onerror = (e) => {
        console.error('SSE connection error:', e);
        eventSource.close(); // Close the connection on error
        displayErrorMessage('Connection lost. Please try sending your message again.');
    };
}

/**
 * Legacy function - now replaced by showVectorStoreManagement() in render.js
 * Kept for backward compatibility if needed
 */
function startEmbedding() {
    console.log('Redirecting to vector store management...');
    showVectorStoreManagement();
}

/**
 * Clears the entire chat conversation
 */
function clearConversation() {
    clearChat();
    console.log('Conversation cleared');
}