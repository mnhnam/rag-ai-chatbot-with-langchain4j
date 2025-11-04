# RAG AI Chatbot with LangChain4j

A comprehensive Spring Boot-based Retrieval-Augmented Generation (RAG) chatbot application using LangChain4j, Ollama LLM, and PostgreSQL with pgvector for vector storage. Features a modern web interface with document management capabilities and real-time streaming chat using functional programming principles.

## Features

- **Intelligent RAG Chatbot**: Context-aware conversations using retrieved knowledge
- **Document Management**: Upload, view, and delete knowledge base documents (.md and .txt files)  
- **Vector Store Integration**: PostgreSQL with pgvector extension for semantic search and document indexing
- **Real-time Streaming**: Server-Sent Events (SSE) for responsive chat experience
- **Modern UI**: Dual-mode interface for chat and document management
- **Drag & Drop Support**: Easy document upload via drag-and-drop functionality
- **Markdown Rendering**: Rich text formatting with syntax highlighting for code blocks
- **Functional Programming**: Clean, maintainable code using functional programming principles
- **Externalized Configuration**: All settings managed via application.properties for different environments
- **Ollama Integration**: Local LLM support with Gemma and Nomic embedding models

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **Docker and Docker Compose** (for PostgreSQL with pgvector)
- **Ollama** (for local LLM and embedding models)
  - Gemma 3 4B model (`gemma3:4b`)
  - Nomic Embed Text model (`nomic-embed-text:latest`)

## Project Structure

```
├── src/main/java/chatbot/chatbot/
│   ├── ChatbotApplication.java              # Main Spring Boot application
│   ├── config/AiConfiguration.java          # Spring configuration for AI services and vector store
│   ├── controller/ChatController.java       # REST API endpoints (chat, files, vector store)
│   ├── handler/StreamChatHandler.java       # Handles streaming chat responses
│   ├── service/
│   │   ├── AiService.java                   # Ollama integration (chat + embeddings)
│   │   └── VectorStoreService.java          # PostgreSQL pgvector store operations
│   ├── prompttemplate/RagPromptTemplate.java # RAG prompt templates
│   ├── textsplitter/SimpleTextSplitter.java # Document chunking utilities
│   └── utils/FileUtils.java                # File management utilities
├── src/main/resources/
│   ├── application.properties               # Application configuration
│   └── static/
│       ├── index.html                      # Dual-mode web UI (chat + document management)
│       └── js/
│           ├── main.js                     # Core application logic and API communication
│           ├── render.js                   # Chat rendering with markdown and syntax highlighting
│           └── render-vector-store.js      # Document management UI and interactions
├── vector-store/PG Vector/                 # PostgreSQL pgvector configuration
│   ├── docker-compose.yml                 # Docker setup for PostgreSQL with pgvector
│   ├── init.sql                           # Database initialization script
│   ├── pgvector.bat                       # Windows script to start PostgreSQL
│   └── SETUP.md                           # Setup instructions
├── raw_data/                               # Document storage (created automatically)
└── pom.xml                                 # Maven dependencies
```

## Getting Started

### 1. Set up PostgreSQL with pgvector

The application uses PostgreSQL with pgvector extension as a vector database for RAG functionality. Start PostgreSQL using Docker:

```bash
cd vector-store/PG\ Vector
docker-compose up -d
```

Or on Windows, use the provided batch script:

```cmd
cd vector-store\PG Vector
pgvector.bat
```

**PostgreSQL Services:**
- PostgreSQL: `localhost:5432`
- Database: `rag_db`
- User: `admin`
- Password: `admin`

### 2. Set up Ollama Models

Install and set up Ollama with the required models:

**Install Ollama:**
Visit [ollama.ai](https://ollama.ai) and download Ollama for your platform.

**Pull Required Models:**

```bash
ollama pull gemma3:4b
ollama pull nomic-embed-text:latest
```

**Configure Ollama Server URL:**
Update `application.properties` if Ollama is running on a different host:

```properties
app.ai.server-url=http://localhost:11434
```

### 3. Build and Run the Application

#### Using Maven Wrapper (Recommended)

**Windows:**
```cmd
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**
```bash
./mvnw clean compile
./mvnw spring-boot:run
```

#### Using System Maven

```bash
mvn clean compile
mvn spring-boot:run
```

### 4. Access the Application

Once the application starts successfully, you can access:

- **Web UI**: http://localhost:8080
- **API Health Check**: http://localhost:8080/actuator/health (if actuator is enabled)

## Using the Application

### Web Interface

1. Open your browser and navigate to [http://localhost:8080](http://localhost:8080)
2. You'll see the **AI Assistant** chat interface with two main modes:

#### Chat Mode (Default)
- Type your message in the input field and click **"Send"**
- The AI will respond with streaming text responses
- Responses support **Markdown formatting** including code blocks with syntax highlighting
- The AI can answer questions based on uploaded documents (RAG functionality)

#### Vector Store Management Mode
1. Click **"Vector store management"** button to switch to document management
2. **Upload Documents**: 
   - Click **"Select Multiple Files"** to choose `.md` or `.txt` files
   - Or **drag and drop** files directly onto the upload area
   - Supported formats: Markdown (`.md`) and Text (`.txt`) files
3. **View Uploaded Files**: See all documents with file size and modification date
4. **Delete Documents**: Click the "Delete" button next to any file to remove it
5. **Create Vector Index**: Click **"Create Index"** to process documents for RAG functionality
6. Click **"Back to Chat"** to return to the chat interface

### API Endpoints

The application exposes comprehensive REST endpoints:

#### Chat Endpoints
- `POST /api/chat` - Send a message and receive a conversation ID
- `GET /api/stream?conversationId={id}` - Stream AI responses via Server-Sent Events

#### Document Management Endpoints
- `POST /api/upload` - Upload a single document file
- `GET /api/files` - Get list of uploaded documents
- `POST /api/deleteFile` - Delete a specific document
- `POST /api/createIndex` - Process documents and create vector store index

#### Example API Usage

```bash
# Send a chat message
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "Hello, can you help me with the uploaded documents?"

# Upload a document
curl -X POST http://localhost:8080/api/upload \
  -F "file=@document.md"

# Get list of uploaded files
curl -X GET http://localhost:8080/api/files

# Create vector store index from uploaded documents
curl -X POST http://localhost:8080/api/createIndex

# Delete a specific file
curl -X POST http://localhost:8080/api/deleteFile \
  -d "fileName=document.md"
```

## Configuration

### Application Properties

The main configuration is in `src/main/resources/application.properties`:

```properties
spring.application.name=chatbot

# Database Configuration
app.database.host=localhost
app.database.port=5432
app.database.name=rag_db
app.database.user=admin
app.database.password=admin
app.database.table=test_index

# Vector Store Configuration
app.vectorstore.min-score=0.7
app.vectorstore.raw-data-dir=raw_data

# AI Service Configuration
app.ai.server-url=http://192.168.72.180:11434
app.ai.chat-model-name=gemma3:4b
app.ai.embedding-model-name=nomic-embed-text:latest
```

### PostgreSQL Configuration

The PostgreSQL setup is configured in `vector-store/PG Vector/docker-compose.yml`. The default configuration:

- Runs on port 5432
- Database: `rag_db`
- User: `admin`
- Password: `admin`
- Includes pgvector extension
- Stores data in a Docker volume

### Ollama Configuration

Configure your Ollama server URL and model names in `application.properties`:

- **Server URL**: `app.ai.server-url` (default: `http://localhost:11434`)
- **Chat Model**: `app.ai.chat-model-name` (default: `gemma3:4b`)
- **Embedding Model**: `app.ai.embedding-model-name` (default: `nomic-embed-text:latest`)

## Development

### Running Tests

```bash
.\mvnw.cmd test
```

### Stopping OpenSearch

To stop the OpenSearch services:

```bash
cd vector-store/OpenSearch
docker-compose down
```

Or on Windows:
```cmd
cd vector-store\OpenSearch
stop-opensearch.bat
```

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**: Change the server port in `application.properties`:
   ```properties
   server.port=8081
   ```

2. **OpenAI API errors**: Ensure your API key is valid and has sufficient credits

3. **OpenSearch connection issues**: Make sure Docker is running and OpenSearch container is healthy:
   ```bash
   docker ps
   curl http://localhost:9200
   ```

4. **Java version issues**: Ensure you're using Java 21 or higher:
   ```bash
   java -version
   ```

## Technologies Used

### Backend

- **Spring Boot 3.4.4** - Main web framework with functional programming principles
- **Spring WebFlux** - Reactive web stack for Server-Sent Events (SSE)
- **LangChain4j 1.7.1** - Java framework for LLM integration and RAG
- **PostgreSQL + pgvector** - Vector database for semantic search and document storage
- **Ollama Gemma 3 4B** - Chat model for conversational AI
- **Ollama Nomic Embed Text** - Embedding model for vector search
- **Functional Programming** - Clean, maintainable code with pure functions and immutable patterns

### Frontend

- **TailwindCSS** - Modern utility-first CSS framework
- **jQuery 3.7.1** - DOM manipulation and AJAX requests
- **Marked.js** - Markdown parsing and rendering
- **Highlight.js** - Syntax highlighting for code blocks (GitHub Dark theme)
- **Modular JavaScript Architecture** - Separated concerns with dedicated render modules

### Infrastructure

- **Docker & Docker Compose** - PostgreSQL with pgvector containerization
- **Maven** - Dependency management and build automation
- **Ollama** - Local LLM server for AI models

## How RAG (Retrieval-Augmented Generation) Works

The application implements a sophisticated RAG pipeline:

### Document Processing Pipeline
1. **Document Upload**: Users upload `.md` or `.txt` files through the web interface
2. **Text Chunking**: Documents are split into manageable chunks with configurable overlap
3. **Embeddings Generation**: Each chunk is converted to vector embeddings using Ollama's `nomic-embed-text` model
4. **Vector Storage**: Embeddings are stored in PostgreSQL with pgvector extension and metadata (source file, chunk index)

### Query Processing Pipeline
1. **User Query**: User submits a question via the chat interface
2. **Query Embedding**: The question is converted to a vector embedding using Ollama's embedding model
3. **Similarity Search**: PostgreSQL with pgvector finds the most relevant document chunks (configurable similarity threshold)
4. **Context Injection**: Relevant chunks are injected into the prompt template
5. **AI Response**: Ollama Gemma 3 generates a response using both the question and retrieved context
6. **Streaming Output**: Response is streamed back to the user in real-time via Server-Sent Events

### Prompt Template System
The application uses structured prompt templates for consistent RAG responses:
- **System Prompt**: Defines the AI's role and behavior
- **User Prompt**: Combines the user's question with retrieved context
- **Context Integration**: Seamlessly weaves retrieved information into responses

## Architecture Overview

### Backend Architecture
- **Controller Layer** (`ChatController`): REST API endpoints with proper error handling
- **Configuration Layer** (`AiConfiguration`): Spring beans for AI services and vector store with externalized configuration
- **Service Layer**: Business logic separation with functional programming principles
  - `AiService`: Ollama integration and model management
  - `VectorStoreService`: PostgreSQL pgvector operations and document processing
- **Utility Layer**: Reusable components
  - `FileUtils`: File validation, storage, and management
  - `SimpleTextSplitter`: Document chunking with configurable parameters
- **Handler Layer** (`StreamChatHandler`): Server-Sent Events management

### Frontend Architecture
- **Modular JavaScript Design**: Separated concerns for maintainability
  - `main.js`: Core application logic and API communication
  - `render.js`: Chat UI rendering with Markdown and syntax highlighting
  - `render-vector-store.js`: Document management interface and file operations
- **Progressive Enhancement**: Works without JavaScript for basic functionality
- **Responsive Design**: TailwindCSS utility classes for modern UI

### Data Flow
1. **File Upload Flow**: Frontend → REST API → File Validation → Disk Storage
2. **Document Processing Flow**: Files → Text Splitting → Embeddings → Vector Store
3. **Chat Flow**: User Input → Query Embedding → Vector Search → Context Retrieval → AI Response → SSE Stream

## Configuration Details

### Vector Store Settings

- **Database URL**: `localhost:5432`
- **Database**: `rag_db`
- **Table**: `test_index`
- **Extension**: pgvector for vector operations
- **Search Threshold**: Configurable minimum similarity score (default: 0.7)

### AI Model Settings

- **Ollama URL**: `http://localhost:11434` (configurable)
- **Chat Model**: `gemma3:4b`
- **Embedding Model**: `nomic-embed-text:latest`
- **Configuration**: All settings externalized via application.properties

### File Processing Settings

- **Supported Formats**: `.md`, `.txt`
- **Storage Location**: Configurable via `app.vectorstore.raw-data-dir` (default: `raw_data/`)
- **Processing**: Automatic chunking and vectorization

## API Documentation

The application provides streaming chat functionality through a comprehensive REST API that supports Server-Sent Events (SSE) for real-time responses and full document lifecycle management.

## Contributing

This is a demo project showcasing modern RAG architecture with Spring Boot and LangChain4j. Feel free to extend and modify for your use cases.

## License

This is a demo project showcasing modern RAG architecture with functional programming principles, Spring Boot, LangChain4j, and Ollama for educational purposes.