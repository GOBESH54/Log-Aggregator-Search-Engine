@echo off
setlocal

echo ========================================
echo Log Aggregator Authenticated Test Script
echo ========================================
echo.

if "%TOKEN%"=="" (
  echo TOKEN environment variable is missing.
  echo First run this command to get a token:
  echo curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"YOUR_USER\",\"password\":\"YOUR_PASSWORD\"}"
  goto :end
)

echo [1/4] Ingesting sample logs...
curl -X POST http://localhost:8080/api/logs/ingest -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "[{\"source\":\"app-server-1\",\"level\":\"ERROR\",\"message\":\"Database connection timeout occurred\"},{\"source\":\"app-server-2\",\"level\":\"INFO\",\"message\":\"User authentication successful\"},{\"source\":\"app-server-1\",\"level\":\"WARN\",\"message\":\"High memory usage detected on server\"}]"
echo.
echo.

echo [2/4] Searching for 'database' keyword...
curl -X POST http://localhost:8080/api/logs/search -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"searchText\":\"database\"}"
echo.
echo.

echo [3/4] Running AI analysis...
curl -X POST http://localhost:8080/api/logs/analyze -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"level\":\"ERROR\",\"prompt\":\"What is the likely root cause?\"}"
echo.
echo.

echo [4/4] Triggering anomaly scan...
curl -X POST http://localhost:8080/api/logs/anomaly/scan -H "Authorization: Bearer %TOKEN%"
echo.

:end
echo.
echo ========================================
echo Test completed
echo ========================================
pause
