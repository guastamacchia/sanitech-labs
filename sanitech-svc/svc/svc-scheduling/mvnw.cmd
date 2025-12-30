\
@ECHO OFF
SETLOCAL

SET WRAPPER_DIR=%~dp0\.mvn\wrapper
SET JAR=%WRAPPER_DIR%\maven-wrapper.jar
SET PROPS=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%JAR%" (
  FOR /F "tokens=1,2 delims==" %%A IN (%PROPS%) DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
  )
  IF "%WRAPPER_URL%"=="" SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

  ECHO Downloading %WRAPPER_URL%
  powershell -Command "Invoke-WebRequest -UseBasicParsing %WRAPPER_URL% -OutFile %JAR%"
)

SET JAVA_EXEC=%JAVA_HOME%\bin\java.exe
IF NOT EXIST "%JAVA_EXEC%" SET JAVA_EXEC=java

"%JAVA_EXEC%" -jar "%JAR%" %*
