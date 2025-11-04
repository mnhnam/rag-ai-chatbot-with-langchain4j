// ===================================
// RENDERING AND UI MANAGEMENT MODULE
// ===================================

marked.setOptions({
    highlight: function(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                return hljs.highlight(code, { language: lang }).value;
            } catch (err) {}
        }
        return hljs.highlightAuto(code).value;
    }
});

/**
 * Renders a user message in the chat window
 * @param {string} message - The user's message text
 */
function renderUserMessage(message) {
    // Escape HTML in user message for security, then parse as markdown
    const escapedMessage = escapeHtml(message);
    
    const messageElement = `
        <div class="flex items-start self-end float-right js-user-message mb-4 max-w-5xl">
            <div class="mr-4">
                <div class="bg-blue-500 p-3 rounded-lg text-white js-message-content prose prose-sm prose-invert max-w-none">${marked.parse(escapedMessage)}</div>
            </div>
            <div class="w-10 min-w-10 h-10 bg-gray-400 rounded-full flex items-center justify-center text-white font-medium text-sm">U</div>
        </div>`;

    document.getElementById('message-list').insertAdjacentHTML('beforeend', messageElement);
    
    // Apply syntax highlighting to user code blocks
    applySyntaxHighlighting('.js-user-message:last-child');
    
    // Auto-scroll to bottom
    scrollToBottom();
}

/**
 * Renders an AI message in the chat window
 * @param {string} message - The AI's message text (can be empty for streaming)
 */
function renderAIMessage(message) {
    // Parse the initial message as markdown (could be empty string)
    const parsedMessage = message ? marked.parse(message) : '';
    
    const messageElement = `
        <div class="flex items-start js-ai-message mb-4 max-w-5xl">
            <div class="w-10 min-w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-medium text-sm">A</div>
            <div class="ml-4">
                <div class="bg-gray-100 p-3 rounded-lg text-gray-800 js-message-content prose prose-sm max-w-none" data-raw-content="${message}">${parsedMessage}</div>
            </div>
        </div>`;

    document.getElementById('message-list').insertAdjacentHTML('beforeend', messageElement);
    
    // Apply syntax highlighting if there are code blocks in the initial message
    if (message) {
        applySyntaxHighlighting('.js-ai-message:last-child');
    }
    
    // Auto-scroll to bottom
    scrollToBottom();
}

/**
 * Updates the latest AI message with streamed content
 * @param {string} content - New content to add/replace
 * @param {boolean} isFinal - Whether this is the final update
 */
function updateAIMessage(content, isFinal) {
    // Select all elements containing AI message content
    const aiMessages = document.querySelectorAll('.js-ai-message .js-message-content');
    if (aiMessages.length > 0) {
        // Get the most recently added AI message
        const lastMessage = aiMessages[aiMessages.length - 1];

        if (isFinal) {
            // If this is the final update, parse the final content as markdown
            lastMessage.innerHTML = marked.parse(content);
            lastMessage.dataset.rawContent = content;
        } else {
            // For streaming updates, append raw content first, then parse as markdown
            // Store the accumulated content in a data attribute
            const currentContent = lastMessage.dataset.rawContent || '';
            const newContent = currentContent + content;
            lastMessage.dataset.rawContent = newContent;
            
            // Parse and display the accumulated content as markdown
            lastMessage.innerHTML = marked.parse(newContent);
        }
        
        // Apply syntax highlighting to any code blocks
        applySyntaxHighlighting(lastMessage);
        
        // Auto-scroll to bottom
        scrollToBottom();
    }
}

/**
 * Escapes HTML characters to prevent XSS attacks
 * @param {string} text - Text to escape
 * @returns {string} - Escaped text
 */
function escapeHtml(text) {
    return text.replace(/[&<>"']/g, function(match) {
        const escapeMap = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#x27;'
        };
        return escapeMap[match];
    });
}

/**
 * Applies syntax highlighting to code blocks in a specific element
 * @param {string|Element} selector - CSS selector or DOM element
 */
function applySyntaxHighlighting(selector) {
    let element;
    if (typeof selector === 'string') {
        element = document.querySelector(selector);
    } else {
        element = selector;
    }
    
    if (element) {
        element.querySelectorAll('pre code').forEach((block) => {
            hljs.highlightElement(block);
        });
    }
}

/**
 * Scrolls the chat window to the bottom
 */
function scrollToBottom() {
    const chatWindow = document.getElementById('chat-window');
    if (chatWindow) {
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }
}

/**
 * Shows a loading indicator for AI responses
 */
function showLoadingIndicator() {
    const loadingElement = `
        <div class="flex items-start js-loading-indicator mb-4 max-w-5xl">
            <div class="w-10 min-w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-medium text-sm">A</div>
            <div class="ml-4">
                <div class="bg-gray-100 p-3 rounded-lg text-gray-800">
                    <div class="flex space-x-1">
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                    </div>
                </div>
            </div>
        </div>`;
    
    document.getElementById('message-list').insertAdjacentHTML('beforeend', loadingElement);
    scrollToBottom();
}

/**
 * Removes the loading indicator
 */
function hideLoadingIndicator() {
    const loadingIndicator = document.querySelector('.js-loading-indicator');
    if (loadingIndicator) {
        loadingIndicator.remove();
    }
}

/**
 * Displays an error message in the chat
 * @param {string} errorMessage - Error message to display
 */
function displayErrorMessage(errorMessage) {
    const errorElement = `
        <div class="flex items-start js-error-message mb-4 max-w-5xl">
            <div class="w-10 min-w-10 h-10 bg-red-500 rounded-full flex items-center justify-center text-white font-medium text-sm">!</div>
            <div class="ml-4">
                <div class="bg-red-100 border border-red-300 p-3 rounded-lg text-red-800">
                    <strong>Error:</strong> ${escapeHtml(errorMessage)}
                </div>
            </div>
        </div>`;
    
    document.getElementById('message-list').insertAdjacentHTML('beforeend', errorElement);
    scrollToBottom();
}

/**
 * Clears all messages from the chat window
 */
function clearChat() {
    const messageList = document.getElementById('message-list');
    if (messageList) {
        messageList.innerHTML = '';
    }
}

/**
 * Injects custom CSS for better highlight.js integration
 */
function injectHighlightStyles() {
    const style = document.createElement('style');
    style.textContent = `
        /* Custom styles for highlight.js code blocks */
        .js-message-content pre {
            margin: 0.75rem 0 !important;
            border-radius: 0.5rem !important;
            overflow-x: auto;
        }
        
        .js-message-content pre code {
            padding: 1rem !important;
            border-radius: 0.5rem !important;
            display: block;
            font-size: 0.875rem;
            line-height: 1.5;
        }
    `;
    
    document.head.appendChild(style);
}

// Initialize rendering module when DOM is ready
$(document).ready(function() {
    // Inject custom styles for highlight.js
    injectHighlightStyles();
    
    console.log('Render module initialized');
});