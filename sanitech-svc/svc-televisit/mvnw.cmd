@ECHO OFF
SETLOCAL
SET MAVEN_PROJECTBASEDIR=%~dp0
IF EXIST "%JAVA_HOME%\bin\java.exe" (
  SET JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET JAVA_CMD=java
)

%JAVA_CMD% -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
