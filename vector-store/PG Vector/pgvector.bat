@echo off
setlocal enabledelayedexpansion

echo Starting PG Vector services...
docker-compose up -d

echo.
echo PG Vector services are starting up...
echo PostgreSQL with pgvector will be available at: localhost:5432
echo PgAdmin will be available at: http://localhost:5050
echo.
echo Default credentials:
echo Database: rag_db
echo Username: admin  
echo Password: admin
echo.
echo PgAdmin credentials:
echo Email: admin@admin.com
echo Password: admin
echo.
echo To connect to PostgreSQL in PgAdmin, use:
echo Host: pgvector
echo Port: 5432
echo Database: rag_db
echo Username: admin
echo Password: admin
echo.
echo ===========================================
echo PG Vector is now running!
echo Type 'shutdown' to stop services or 'delete' to stop services and remove all data.
echo ===========================================
echo.

:input_loop
set /p user_input="Command: "

if /i "!user_input!"=="shutdown" (
    echo.
    echo Stopping PG Vector services...
    docker-compose down
    echo.
    echo PG Vector services have been stopped.
    echo Terminal will close in 3 seconds...
    timeout /t 3 /nobreak >nul
    exit /b 0
) else if /i "!user_input!"=="status" (
    echo.
    echo Checking service status...
    docker-compose ps
    echo.
) else if /i "!user_input!"=="logs" (
    echo.
    echo Recent logs:
    docker-compose logs --tail=20
    echo.
) else if /i "!user_input!"=="delete" (
    echo.
    echo WARNING: This will delete ALL database data and cannot be undone!
    set /p confirm="Are you sure? Type 'yes' to confirm: "
    if /i "!confirm!"=="yes" (
        echo.
        echo Stopping PG Vector services and removing all data...
        docker-compose down -v
        echo.
        echo All database data has been deleted.
        echo Terminal will close in 3 seconds...
        timeout /t 3 /nobreak >nul
        exit /b 0
    ) else (
        echo.
        echo Delete operation cancelled.
        echo.
    )
) else if /i "!user_input!"=="help" (
    echo.
    echo Available commands:
    echo   shutdown - Stop services and exit
    echo   delete   - Stop services and DELETE ALL data (WARNING: irreversible!)
    echo   status   - Show service status
    echo   logs     - Show recent logs
    echo   help     - Show this help
    echo.
) else if "!user_input!"=="" (
    rem Do nothing for empty input
) else (
    echo.
    echo Unknown command: !user_input!
    echo Type 'shutdown' to stop services or 'delete' to stop services and remove all data.
    echo.
)

goto input_loop