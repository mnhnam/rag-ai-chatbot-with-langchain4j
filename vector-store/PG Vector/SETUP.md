# PG Vector Setup

## Start PG Vector Services

```bash
# Run the interactive script
pgvector.bat

# Type 'shutdown' to stop services and exit
```

## Access UI

- **PgAdmin Web Interface**: `http://localhost:5050`
- **Account**: `admin`
- **Password**: `admin`

## Database Connection (in PgAdmin)

- **Host**: `pgvector`
- **Port**: `5432`
- **Database**: `rag_db`
- **Username**: `admin`
- **Password**: `admin`

## Search Data by Index Name

```sql
-- List all indexes
SELECT indexname FROM pg_indexes WHERE tablename = 'your_table_name';

-- Search for specific index by name
SELECT * FROM pg_indexes WHERE indexname = 'your_index_name';

-- View index details
\d your_index_name
```
