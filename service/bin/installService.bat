set SERVICE_NAME=jctrl
set PR_INSTALL=C:\programs\jctrl\service\jctrl.exe
 
REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=C:\programs\jctrl\service\logs
set PR_STDOUTPUT=C:\programs\jctrl\service\logs\stdout.txt
set PR_STDERROR=C:\programs\jctrl\service\logs\stderr.txt
set PR_LOGLEVEL=Info

REM Path to java installation
set PR_JVM=C:\programs\java\jdk10\bin\server\jvm.dll
set PR_CLASSPATH=C:\programs\jctrl\jctrl-service-0.0.1-SNAPSHOT-jar-with-dependencies.jar
 
REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=com.github.gv2011.jctrl.service.Main
set PR_STARTMETHOD=run
 
REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=com.github.gv2011.jctrl.service.Main
set PR_STOPMETHOD=stop
 
REM Install service
jctrl.exe install %SERVICE_NAME%