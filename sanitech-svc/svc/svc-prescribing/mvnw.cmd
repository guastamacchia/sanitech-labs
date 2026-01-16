\
@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
SET WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
SET PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties
SET JAR_FILE=%WRAPPER_DIR%\maven-wrapper.jar

IF NOT EXIST "%PROPS_FILE%" (
  ECHO Missing %PROPS_FILE%
  EXIT /B 1
)

FOR /F "tokens=2 delims==" %%A IN ('findstr /B "wrapperUrl=" "%PROPS_FILE%"') DO SET WRAPPER_URL=%%A

IF "%WRAPPER_URL%"=="" (
  SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
)

IF NOT EXIST "%JAR_FILE%" (
  IF NOT EXIST "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  ECHO Downloading Maven Wrapper from %WRAPPER_URL%
  powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%','%JAR_FILE%')"
)

IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_EXE=java
)

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%JAR_FILE%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
