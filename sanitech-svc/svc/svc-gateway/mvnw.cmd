@echo off
set BASE_DIR=%~dp0
set WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo maven-wrapper.jar not found. Please run on a machine with internet access to download the wrapper jar.
)

java -jar "%WRAPPER_JAR%" %*
