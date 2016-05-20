:: Suppress initialization output
@echo off
@title Data Recorder

:: Use these opts for remote debugging
::@set JAVA_OPTS=-Xms256m -Xmx512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

:: Virtual Machine Arguments
set VMARGS=
set VMARGS=%VMARGS% -Dpublisher.suppress.start.timestamp=false
set VMARGS=%VMARGS% -Dsubscriber.insert.start.timestamp=false

:: Classpath
@set CLASSPATH=
set CLASSPATH=..\conf\
FOR %%f IN ( "..\lib\*.jar" ) DO CALL SET CLASSPATH=%%CLASSPATH%%;"%%f"
FOR %%f IN ( "..\lib\ext\*.jar" ) DO CALL SET CLASSPATH=%%CLASSPATH%%;"%%f"

:: Class to execute
@set CLASS=com.datarecorder.DataRecorder
echo %CLASSPATH%
IF DEFINED JAVA_HOME (
   "%JAVA_HOME%\bin\java" %JAVA_OPTS% %VMARGS% -cp %CLASSPATH% %CLASS%

) ELSE (
       echo JAVA_HOME must be defined.
       echo Example: set JAVA_HOME=c:\java\j2sdk1.5.0_11
       pause
)
