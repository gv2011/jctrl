bin\prunsrv.exe install jctrl --Startup=auto --LogPath=%cd%\log ^
--ServiceUser=LocalSystem ^
--StartMode=exe ^
--StartPath=%cd% ^
--StartImage=%cd%\bin\javaw.exe ^
--StartParams=-DLOG_PREFIX=RUN9- ^
++StartParams=-m ++StartParams=com.github.gv2011.jctrl.service/com.github.gv2011.jctrl.service.Main ^
++StartParams=RUN9 ^
--StopMode=exe ^
--StopPath=%cd% ^
--StopImage=%cd%\bin\javaw.exe ^
--StopParams=-DLOG_PREFIX=STOP- ^
++StopParams=-m ++StopParams=com.github.gv2011.jctrl.service/com.github.gv2011.jctrl.service.Main ^
++StopParams=STOP
bin\prunsrv.exe start jctrl
