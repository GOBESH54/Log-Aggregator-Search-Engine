@echo off
echo === Installing BFF dependencies ===
cd bff
call npm install
cd ..

echo === Installing Frontend dependencies ===
cd frontend
call npm install
cd ..

echo === Starting BFF (port 3001) ===
start "BFF" cmd /k "cd bff && npm run dev"

echo === Starting Frontend (port 5173) ===
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo BFF:      http://localhost:3001
echo Frontend: http://localhost:5173
echo Spring:   http://localhost:8080  (must be running separately)
