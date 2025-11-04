# RAG AI Chatbot with LangChain4j

A Spring Boot-based Retrieval-Augmented Generation (RAG) chatbot application using LangChain4j, OpenAI LLM, and PostgreSQL with pgvector for vector storage.

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **Docker and Docker Compose** (for PostgreSQL with pgvector)
- **OpenAI API Key** (for LLM and embedding models)
  - GPT-4.1 Mini model (`gpt-4.1-mini`) for chat
  - Text Embedding 3 Large  model (`text-embedding-3-large`) for embeddings

## Project Structure

```plain
├── src/main/java/chatbot/chatbot/
│   ├── ChatbotApplication.java              # Main Spring Boot application
│   ├── config/AiConfiguration.java          # Spring configuration for AI services and vector store
│   ├── controller/ChatController.java       # REST API endpoints (chat, files, vector store)
│   ├── handler/StreamChatHandler.java       # Handles streaming chat responses
│   ├── service/
│   │   ├── AiService.java                   # OpenAI integration (chat + embeddings)
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

### 2. Set up OpenAI API Key

Configure your OpenAI API key as an environment variable:

**Windows (PowerShell):**

```powershell
$env:OPENAI_API_KEY="your-api-key-here"
```

**Windows (Command Prompt):**

```batch
set OPENAI_API_KEY=your-api-key-here
```

**Linux/macOS:**

```bash
export OPENAI_API_KEY="your-api-key-here"
```

Alternatively, you can update `application.properties` directly:

```properties
app.ai.api-key=your-api-key-here
```

### 4. Build and Run the Application using Maven

```bash
mvn clean compile
mvn spring-boot:run
```

### 5. Access the Application

Once the application starts successfully, you can access:

- **Web UI**: http://localhost:8080`
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

### Customizing LLM and Embedding Models

You can change the language model and embedding model by modifying the `app.ai.chat-model-name` and `app.ai.embedding-model-name` properties in the configuration file.

**Available OpenAI models**:

- **Chat Models**: Support all popular OpenAI model such as `gpt-4o`, `gpt-4.1-mini`, `gpt-5`,...
- **Embedding Models**: `text-embedding-3-large`, `text-embedding-3-small`, `text-embedding-ada-002`

Refer to the [OpenAI Models Documentation](https://platform.openai.com/docs/models) for the latest available models and their capabilities.

## Technologies Used

### Backend

- **Spring Boot 3.4.4** - Main web framework with functional programming principles
- **Spring WebFlux** - Reactive web stack for Server-Sent Events (SSE)
- **LangChain4j 1.7.1** - Java framework for LLM integration and RAG
- **PostgreSQL + pgvector** - Vector database for semantic search and document storage
- **OpenAI GPT-4.1 Mini** - Chat model for conversational AI
- **OpenAI Text Embedding 3 Large** - Embedding model for vector search

### Frontend

- **TailwindCSS** - Modern utility-first CSS framework
- **jQuery 3.7.1** - DOM manipulation and AJAX requests
- **Marked.js** - Markdown parsing and rendering
- **Highlight.js** - Syntax highlighting for code blocks (GitHub Dark theme)
- **Modular JavaScript Architecture** - Separated concerns with dedicated render modules

### Infrastructure

- **Docker & Docker Compose** - PostgreSQL with pgvector containerization
- **Maven** - Dependency management and build automation
- **OpenAI API** - Cloud-based LLM and embedding services

## How RAG (Retrieval-Augmented Generation) Works

The application implements a sophisticated RAG pipeline:

### Document Processing Pipeline

1. **Document Upload**: Users upload `.md` or `.txt` files through the web interface
2. **Text Chunking**: Documents are split into manageable chunks with configurable overlap
3. **Embeddings Generation**: Each chunk is converted to vector embeddings using OpenAI's `text-embedding-3-large` model
4. **Vector Storage**: Embeddings are stored in PostgreSQL with pgvector extension and metadata (source file, chunk index)

### Query Processing Pipeline

1. **User Query**: User submits a question via the chat interface
2. **Query Embedding**: The question is converted to a vector embedding using OpenAI's embedding model
3. **Similarity Search**: PostgreSQL with pgvector finds the most relevant document chunks (configurable similarity threshold)
4. **Context Injection**: Relevant chunks are injected into the prompt template
5. **AI Response**: OpenAI GPT-4.1 Mini generates a response using both the question and retrieved context
6. **Streaming Output**: Response is streamed back to the user in real-time via Server-Sent Events

### Prompt Template System

The application uses structured prompt templates for consistent RAG responses:

- **System Prompt**: Defines the AI's role and behavior
- **User Prompt**: Combines the user's question with retrieved context
- **Context Integration**: Seamlessly weaves retrieved information into responses
