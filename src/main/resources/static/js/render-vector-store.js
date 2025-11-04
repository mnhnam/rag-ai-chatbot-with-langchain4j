/**
 * Escapes HTML characters to prevent XSS attacks
 * @param {string} text - Text to escape
 * @returns {string} - Escaped text
 */
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

/**
 * Shows the vector store management interface and hides the chat
 */
function showVectorStoreManagement() {
    const chatContainer = document.getElementById('chat-container');
    const vectorStoreContainer = document.getElementById('vector-store-container');
    
    if (chatContainer && vectorStoreContainer) {
        chatContainer.classList.add('hidden');
        vectorStoreContainer.classList.remove('hidden');
    }
    
    console.log('Switched to Vector Store Management');
}

/**
 * Shows the chat interface and hides the vector store management
 */
function showChatInterface() {
    const chatContainer = document.getElementById('chat-container');
    const vectorStoreContainer = document.getElementById('vector-store-container');
    
    if (chatContainer && vectorStoreContainer) {
        vectorStoreContainer.classList.add('hidden');
        chatContainer.classList.remove('hidden');
    }
    
    console.log('Switched to Chat Interface');
}

/**
 * Handles real file upload to the backend API
 */
function handleFileUpload() {
    const fileInput = document.getElementById('file-upload');
    if (fileInput && fileInput.files.length > 0) {
        uploadFilesToServer(Array.from(fileInput.files));
        fileInput.value = ''; // Clear the input
    }
}

/**
 * Uploads multiple files to the server using the /api/upload endpoint
 * Backend expects one file per request, so we upload each file individually
 * @param {File[]} files - Array of files to upload
 */
function uploadFilesToServer(files) {
    if (files.length === 0) {
        showNotification('No files selected', 'error');
        return;
    }
    
    // Show uploading notification
    showNotification(`Uploading ${files.length} file(s)...`, 'info');
    
    let successCount = 0;
    let errorCount = 0;
    let errorMessages = [];
    
    // Upload each file individually
    files.forEach((file, index) => {
        const formData = new FormData();
        formData.append('file', file); // Backend expects 'file' parameter name
        
        fetch('/api/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                successCount++;
                console.log(`File ${file.name} uploaded successfully`);
            } else {
                errorCount++;
                const errors = data.errors || [data.error || 'Upload failed'];
                errorMessages.push(`${file.name}: ${errors.join(', ')}`);
                console.error(`Failed to upload ${file.name}:`, errors);
            }
            
            // Check if all uploads are complete
            if (successCount + errorCount === files.length) {
                // All uploads finished, show final result
                if (errorCount === 0) {
                    showNotification(`All ${successCount} file(s) uploaded successfully!`, 'success');
                } else if (successCount === 0) {
                    showNotification(`All uploads failed: ${errorMessages.join('; ')}`, 'error');
                } else {
                    showNotification(`${successCount} file(s) uploaded, ${errorCount} failed: ${errorMessages.join('; ')}`, 'error');
                }
                
                // Refresh the file list after uploads complete
                loadUploadedFiles();
            }
        })
        .catch(error => {
            errorCount++;
            errorMessages.push(`${file.name}: Network error`);
            console.error(`Upload error for ${file.name}:`, error);
            
            // Check if all uploads are complete
            if (successCount + errorCount === files.length) {
                // All uploads finished, show final result
                if (successCount === 0) {
                    showNotification(`All uploads failed: ${errorMessages.join('; ')}`, 'error');
                } else {
                    showNotification(`${successCount} file(s) uploaded, ${errorCount} failed: ${errorMessages.join('; ')}`, 'error');
                }
                
                // Refresh the file list after uploads complete
                loadUploadedFiles();
            }
        });
    });
}

/**
 * Loads and displays the list of uploaded files from the server
 */
function loadUploadedFiles() {
    fetch('/api/files')
    .then(response => response.json())
    .then(data => {
        const filesList = document.getElementById('uploaded-files-list');
        if (!filesList) return;
        
        // Clear existing list
        filesList.innerHTML = '';
        
        if (data.success && data.files) {
            data.files.forEach(fileInfo => {
                addUploadedFileToList(fileInfo);
            });
        } else if (data.message) {
            // No files found
            filesList.innerHTML = `
                <div class="text-center p-4 text-gray-500">
                    <p>No files uploaded yet</p>
                </div>`;
        }
    })
    .catch(error => {
        console.error('Error loading files:', error);
        showNotification('Failed to load files list', 'error');
    });
}

/**
 * Adds a file to the uploaded files list
 * @param {Object} fileInfo - File information from the server
 */
function addUploadedFileToList(fileInfo) {
    const filesList = document.getElementById('uploaded-files-list');
    const fileExtension = fileInfo.name.split('.').pop().toLowerCase();
    const iconColor = fileExtension === 'md' ? 'blue' : 'green';
    
    const fileElement = `
        <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg animate-fadeIn" data-filename="${escapeHtml(fileInfo.name)}">
            <div class="flex items-center space-x-3">
                <div class="w-8 h-8 bg-${iconColor}-100 rounded-full flex items-center justify-center">
                    <svg class="w-4 h-4 text-${iconColor}-600" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm0 2h12v10H4V5z"/>
                    </svg>
                </div>
                <div>
                    <p class="text-sm font-medium text-gray-900">${escapeHtml(fileInfo.name)}</p>
                    <p class="text-xs text-gray-500">${fileInfo.uploadTime || 'Uploaded'} • ${fileInfo.sizeFormatted || fileInfo.size + ' bytes'}</p>
                </div>
            </div>
            <button onclick="removeFile(this, '${escapeHtml(fileInfo.name)}')" class="text-red-600 hover:text-red-800 text-sm font-medium transition-colors duration-200">Remove</button>
        </div>`;
    
    if (filesList) {
        filesList.insertAdjacentHTML('beforeend', fileElement);
    }
}

/**
 * Removes a file from the server and updates the UI
 * @param {HTMLElement} button - The remove button that was clicked
 * @param {string} fileName - The name of the file to remove
 */
function removeFile(button, fileName) {
    if (!fileName) {
        // Extract filename from the element if not provided
        const fileElement = button.closest('.flex');
        fileName = fileElement?.getAttribute('data-filename');
    }
    
    if (!fileName) {
        showNotification('Could not determine file name to delete', 'error');
        return;
    }
    
    // Send delete request to server
    const formData = new FormData();
    formData.append('fileName', fileName);
    
    fetch('/api/deleteFile', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove from UI with animation
            const fileElement = button.closest('.flex');
            if (fileElement) {
                fileElement.classList.add('opacity-50', 'scale-95', 'transition-all', 'duration-200');
                setTimeout(() => {
                    fileElement.remove();
                }, 200);
            }
            showNotification('File removed successfully', 'success');
        } else {
            const errors = data.errors || [data.error || 'Delete failed'];
            showNotification(`Delete failed: ${errors.join(', ')}`, 'error');
        }
    })
    .catch(error => {
        console.error('Delete error:', error);
        showNotification('Delete failed due to network error', 'error');
    });
}

/**
 * Rebuilds the vector index by calling the backend API
 */
function rebuildIndex() {
    const rebuildBtn = document.getElementById('rebuild-index-btn');
    if (!rebuildBtn) return;
    
    const originalText = rebuildBtn.textContent;
    rebuildBtn.textContent = 'Processing...';
    rebuildBtn.disabled = true;
    rebuildBtn.classList.add('opacity-75', 'cursor-not-allowed');
    
    fetch('/api/createIndex', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        rebuildBtn.textContent = originalText;
        rebuildBtn.disabled = false;
        rebuildBtn.classList.remove('opacity-75', 'cursor-not-allowed');
        
        if (data.success) {
            showNotification('Vector index created successfully!', 'success');
        } else {
            showNotification(`Index creation failed: ${data.error || 'Unknown error'}`, 'error');
        }
    })
    .catch(error => {
        console.error('Index creation error:', error);
        rebuildBtn.textContent = originalText;
        rebuildBtn.disabled = false;
        rebuildBtn.classList.remove('opacity-75', 'cursor-not-allowed');
        showNotification('Index creation failed due to network error', 'error');
    });
}

/**
 * Resets the vector index by calling the backend API
 */
function resetIndex() {
    const resetBtn = document.getElementById('reset-index-btn');
    if (!resetBtn) return;
    
    // Show confirmation dialog
    if (!confirm('Are you sure you want to reset the vector index? This will remove all indexed documents and you will need to recreate the index.')) {
        return;
    }
    
    const originalText = resetBtn.textContent;
    resetBtn.textContent = 'Resetting...';
    resetBtn.disabled = true;
    resetBtn.classList.add('opacity-75', 'cursor-not-allowed');
    
    fetch('/api/resetIndex', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        resetBtn.textContent = originalText;
        resetBtn.disabled = false;
        resetBtn.classList.remove('opacity-75', 'cursor-not-allowed');
        
        if (data.success) {
            showNotification('Vector index reset successfully!', 'success');
        } else {
            showNotification(`Index reset failed: ${data.error || 'Unknown error'}`, 'error');
        }
    })
    .catch(error => {
        console.error('Index reset error:', error);
        resetBtn.textContent = originalText;
        resetBtn.disabled = false;
        resetBtn.classList.remove('opacity-75', 'cursor-not-allowed');
        showNotification('Index reset failed due to network error', 'error');
    });
}

/**
 * Shows a notification message
 * @param {string} message - The notification message
 * @param {string} type - The notification type ('success', 'error', 'info')
 */
function showNotification(message, type = 'info') {
    const colors = {
        success: 'bg-green-100 border-green-300 text-green-800',
        error: 'bg-red-100 border-red-300 text-red-800',
        info: 'bg-blue-100 border-blue-300 text-blue-800'
    };
    
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 p-4 rounded-lg border ${colors[type]} shadow-lg z-50 transition-all duration-300 transform translate-x-0`;
    notification.innerHTML = `
        <div class="flex items-center space-x-2">
            <span>${escapeHtml(message)}</span>
            <button onclick="this.parentElement.parentElement.remove()" class="ml-2 text-gray-500 hover:text-gray-700">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                </svg>
            </button>
        </div>`;
    
    document.body.appendChild(notification);
    
    // Auto-remove after 3 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.classList.add('translate-x-full', 'opacity-0');
            setTimeout(() => notification.remove(), 300);
        }
    }, 3000);
}

/**
 * Clears all uploaded files by deleting them one by one
 */
function clearAllDocuments() {
    const filesList = document.getElementById('uploaded-files-list');
    if (!filesList) return;
    
    const fileElements = filesList.querySelectorAll('.flex[data-filename]');
    if (fileElements.length === 0) {
        showNotification('No files to clear', 'info');
        return;
    }
    
    let deletedCount = 0;
    let failedCount = 0;
    
    // Delete each file
    fileElements.forEach((element, index) => {
        const fileName = element.getAttribute('data-filename');
        if (!fileName) return;
        
        setTimeout(() => {
            const formData = new FormData();
            formData.append('fileName', fileName);
            
            fetch('/api/deleteFile', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    deletedCount++;
                    element.classList.add('opacity-0', 'scale-95', 'transition-all', 'duration-200');
                    setTimeout(() => element.remove(), 200);
                } else {
                    failedCount++;
                    console.error('Failed to delete file:', fileName, data.errors);
                }
                
                // Show final notification when all deletions are processed
                if (deletedCount + failedCount === fileElements.length) {
                    if (failedCount === 0) {
                        showNotification(`All ${deletedCount} files cleared successfully!`, 'success');
                    } else {
                        showNotification(`${deletedCount} files cleared, ${failedCount} failed to delete`, 'error');
                    }
                }
            })
            .catch(error => {
                failedCount++;
                console.error('Delete error for file:', fileName, error);
                
                if (deletedCount + failedCount === fileElements.length) {
                    showNotification(`${deletedCount} files cleared, ${failedCount} failed to delete`, 'error');
                }
            });
        }, index * 200); // Stagger the deletions
    });
}

// Initialize vector store module when DOM is ready
$(document).ready(function() {
    console.log('Vector Store Management module initialized');
    
    // Add event listener for file upload
    const fileInput = document.getElementById('file-upload');
    if (fileInput) {
        fileInput.addEventListener('change', handleFileUpload);
    }
    
    // Add drag and drop functionality
    const dropZone = document.querySelector('.border-dashed');
    if (dropZone) {
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('border-purple-400', 'bg-purple-50');
        });
        
        dropZone.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dropZone.classList.remove('border-purple-400', 'bg-purple-50');
        });
        
        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('border-purple-400', 'bg-purple-50');
            
            const files = Array.from(e.dataTransfer.files);
            const validFiles = files.filter(file => 
                file.name.endsWith('.md') || file.name.endsWith('.txt')
            );
            
            if (validFiles.length > 0) {
                uploadFilesToServer(validFiles);
            }
        });
    }
    
    // Load existing files on page load
    loadUploadedFiles();
    
    // Add click handlers for buttons with specific IDs (more reliable than text content)
    $(document).on('click', '#rebuild-index-btn', rebuildIndex);
    $(document).on('click', '#reset-index-btn', resetIndex);
    $(document).on('click', '#clear-all-btn', clearAllDocuments);
});