# RAG AI Chatbot with LangChain4j

A Spring Boot-based Retrieval-Augmented Generation (RAG) chatbot application using LangChain4j, Ollama LLM, and PostgreSQL with pgvector for vector storage.

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **Docker and Docker Compose** (for PostgreSQL with pgvector)
- **Ollama** (for local LLM and embedding models)
  - Gemma 3 4B model (`gemma3:4b`)
  - Nomic Embed Text model (`nomic-embed-text:latest`)

**Note**: To use `gemma3:4b`, your system need at least 8GB RAM and a GPU with at least 4GB of VRAM, such as GTX 1650 4GB or better.

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

## Setup

### 1. Set up PG Vector

#### Start PG Vector Services

```bash
# Run the interactive script
pgvector.bat

# Type 'shutdown' to stop services and exit
```

#### Access UI

- **PgAdmin Web Interface**: `http://localhost:5050`
- **Account**: `admin`
- **Password**: `admin`

#### Database Connection (in PgAdmin)

- **Host**: `pgvector`
- **Port**: `5432`
- **Database**: `rag_db`
- **Username**: `admin`
- **Password**: `admin`

#### Search Data by Index Name

```sql
-- List all indexes
SELECT indexname FROM pg_indexes WHERE tablename = 'your_table_name';

-- Search for specific index by name
SELECT * FROM pg_indexes WHERE indexname = 'your_index_name';

-- View index details
\d your_index_name
```

### 2. Set up Ollama Models

Please read [Ollama installation guide](https://www.ralgar.one/ollama-on-windows-a-beginners-guide/) for more information.

**Configure Ollama Server URL:**
Update `application.properties` with the URL to access Ollama:

```properties
app.ai.server-url=http://localhost:11434
```

### 3. Build and Run the Application using Maven

```bash
mvn clean compile
mvn spring-boot:run
```

### 4. Access the Application

Once the application starts successfully, you can access:

- **Web UI**: http://localhost:8080
- **PG Vector UI**: http://localhost:5050

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
app.ai.server-url=
app.ai.chat-model-name=gemma3:4b
app.ai.embedding-model-name=nomic-embed-text:latest
```

### Customizing LLM and Embedding Models

You can change the language model and embedding model by modifying the `app.ai.chat-model-name` and `app.ai.embedding-model-name` properties in the configuration file.

**Available models** can be found on the [Ollama Library](https://ollama.com/library).

⚠️ **Important**: Different models have different system requirements. Larger models consume significantly more RAM and disk space, and running them can be computationally expensive:

- **Smaller models** (2B-4B parameters) - Suitable for most systems with 8GB+ RAM
- **Medium models** (7B-13B parameters) - Require 16GB+ RAM and are more resource-intensive
- **Large models** (20B+ parameters) - Require 32GB+ RAM and specialized hardware (GPU recommended)

**Before switching to a different model**, ensure your system has adequate:

- **RAM** - The model needs to be loaded into memory during inference
- **Disk Space** - For storing the model files (check Ollama library for model sizes)
- **GPU** (optional) - Recommended for better performance, especially for larger models

Refer to the [Ollama documentation](https://docs.ollama.com/) for detailed system requirements and model comparisons.

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
