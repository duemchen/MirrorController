set SERVICE_NAME=MQTT-Regler
set PR_Description=Empfaengt die IstPositionen und positioniert die Spiegel per MQTT


set LW=%~d0
set STARTDIR=%~dp0
%LW%
cd %STARTDIR%
echo %dir%



rem set PR_INSTALL=F:\NetBeansProjekte\Bildholer\dienst\prunsrv.exe
set PR_INSTALL=%STARTDIR%\prunsrv.exe
 
REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=.\logs
set PR_STDOUTPUT=.\logs\stdout.txt
set PR_STDERROR=.\logs\stderr.txt
set PR_LOGLEVEL=Error
 
REM Path to java installation
set PR_JVM=D:\Programme\java\jdk1.8.0_102\jre\bin\server\jvm.dll
set PR_CLASSPATH=mavenMirrorController-1.jar
 
REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=de.lichtmagnet.mavenmirrorcontroller.ReglerService
set PR_STARTMETHOD=start
 
REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=de.lichtmagnet.mavenmirrorcontroller.ReglerService
set PR_STOPMETHOD=stop
 
REM JVM configuration
set PR_JVMMS=256
set PR_JVMMX=1024
set PR_JVMSS=4000
set PR_JVMOPTIONS=-Duser.language=DE;-Duser.region=de
 
REM Install service
%PR_INSTALL% //IS//%SERVICE_NAME%

pause