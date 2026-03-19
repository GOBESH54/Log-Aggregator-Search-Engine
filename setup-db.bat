@echo off
echo ========================================
echo PostgreSQL Database Setup
echo ========================================
echo.

echo This script will create the 'logdb' database.
echo Make sure PostgreSQL is installed and running.
echo.

set /p PGPATH="Enter PostgreSQL bin path (e.g., C:\Program Files\PostgreSQL\15\bin): "
set /p PGPASS="Enter PostgreSQL password: "

echo.
echo Creating database...
"%PGPATH%\psql.exe" -U postgres -c "CREATE DATABASE logdb;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Database 'logdb' created successfully!
    echo.
    echo Next steps:
    echo 1. Run: mvn spring-boot:run
    echo 2. Run: test.bat
) else (
    echo.
    echo ✗ Failed to create database.
    echo Please check:
    echo - PostgreSQL is installed and running
    echo - Password is correct
    echo - Path to psql.exe is correct
)

echo.
pause
