set SERVICE_NAME=MQTT-Regler

set LW=%~d0
set STARTDIR=%~dp0
%LW%
cd %STARTDIR%
echo %dir%
set PR_INSTALL=%STARTDIR%\prunsrv.exe
%PR_INSTALL% //DS//%SERVICE_NAME%

pause