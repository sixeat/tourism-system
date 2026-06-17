@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ============================================================
rem Tourism System One-Click Runner (Local Machine)
rem Steps: package -> deploy to Tomcat -> start -> open browser
rem ============================================================

set "PROJECT_DIR=%~dp0"
set "JDK_HOME=YOUR_JDK_PATH"
set "TOMCAT_HOME=YOUR_TOMCAT_PATH"
set "APP_NAME=tourism-system"
set "APP_URL=http://localhost:8080/%APP_NAME%/"

rem Local MySQL config.
set "DB_URL=jdbc:mysql://localhost:3306/tourism_system?useUnicode=true^&characterEncoding=UTF-8^&serverTimezone=Asia/Shanghai"
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_MYSQL_PASSWORD"

rem AI API is disabled by default. Fill AI_API_KEY and set enabled=true if needed.
set "AI_API_ENABLED=false"
set "AI_API_URL=https://api.deepseek.com/chat/completions"
set "AI_API_MODEL=deepseek-v4-flash"
set "AI_API_KEY="

if not exist "%JDK_HOME%\bin\java.exe" (
  echo [ERROR] JDK not found: %JDK_HOME%
  pause
  exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\startup.bat" (
  echo [ERROR] Tomcat not found: %TOMCAT_HOME%
  pause
  exit /b 1
)

cd /d "%PROJECT_DIR%"
if not exist "pom.xml" (
  echo [ERROR] pom.xml not found in: %PROJECT_DIR%
  pause
  exit /b 1
)

set "JAVA_HOME=%JDK_HOME%"
set "JRE_HOME=%JDK_HOME%"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
set "PATH=%JDK_HOME%\bin;%PATH%"

echo.
echo [1/5] Stop old Tomcat process...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*org.apache.catalina.startup.Bootstrap*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"
timeout /t 2 /nobreak >nul

echo.
echo [2/5] Build WAR with Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
  echo [ERROR] Maven build failed.
  pause
  exit /b 1
)

echo.
echo [3/5] Deploy WAR to Tomcat...
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%" rmdir /s /q "%TOMCAT_HOME%\webapps\%APP_NAME%"
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%.war" del /f /q "%TOMCAT_HOME%\webapps\%APP_NAME%.war"
copy /y "%PROJECT_DIR%target\%APP_NAME%.war" "%TOMCAT_HOME%\webapps\%APP_NAME%.war" >nul
if errorlevel 1 (
  echo [ERROR] WAR copy failed.
  pause
  exit /b 1
)

echo.
echo [4/5] Start Tomcat...
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo [5/5] Wait and open browser...
timeout /t 8 /nobreak >nul
start "" "%APP_URL%"

echo.
echo Done: %APP_URL%
echo User: demo / demo123
echo Admin: admin / admin123
echo.
pause
