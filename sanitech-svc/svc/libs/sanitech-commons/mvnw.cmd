@echo off
setlocal

set BASE_DIR=%~dp0
set WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
set JAR=%WRAPPER_DIR%\maven-wrapper.jar
set PROPS=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%PROPS%" (
  echo Missing %PROPS%
  exit /b 1
)

for /f "tokens=2 delims==" %%A in ('findstr /b "wrapperUrl=" "%PROPS%"') do set WRAPPER_URL=%%A

if not exist "%JAR%" (
  echo Downloading Maven Wrapper jar...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
    "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%','%JAR%')"
)

java -jar "%JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" %*
endlocal
