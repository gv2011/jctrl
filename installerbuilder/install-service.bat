bin\prunsrv.exe install jctrl --Startup=auto --LogPath=C:\Dateien\src\jctrl\installerbuilder\image\log ^
--ServiceUser=LocalSystem ^
--StartMode=exe ^
--StartPath=C:\Dateien\src\jctrl\installerbuilder\image ^
--StartImage=C:\Dateien\src\jctrl\installerbuilder\image\bin\javaw.exe ^
--StartParams=-DLOG_PREFIX=RUN9- ^
++StartParams=-m ++StartParams=com.github.gv2011.jctrl.service/com.github.gv2011.jctrl.service.Main ^
++StartParams=RUN9 ^
--StopMode=exe ^
--StopPath=C:\Dateien\src\jctrl\installerbuilder\image ^
--StopImage=C:\Dateien\src\jctrl\installerbuilder\image\bin\javaw.exe ^
--StopParams=-DLOG_PREFIX=STOP- ^
++StopParams=-m ++StopParams=com.github.gv2011.jctrl.service/com.github.gv2011.jctrl.service.Main ^
++StopParams=STOP
