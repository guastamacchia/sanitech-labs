@ECHO OFF
SETLOCAL
SET BASEDIR=%~dp0
IF EXIST "%BASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  IF NOT "%JAVA_HOME%"=="" (
    "%JAVA_HOME%\bin\java" -jar "%BASEDIR%\.mvn\wrapper\maven-wrapper.jar" %*
  ) ELSE (
    java -jar "%BASEDIR%\.mvn\wrapper\maven-wrapper.jar" %*
  )
) ELSE (
  ECHO maven-wrapper.jar non trovato. Usa 'mvn ...' oppure rigenera il wrapper con:
  ECHO   mvn -N io.takari:maven:wrapper
  mvn %*
)
ENDLOCAL
