cd target\jlink-service
bin\java.exe ^
-agentlib:jdwp=transport=dt_socket,server=y,address=8000 ^
-DLOG_PREFIX=RUN9- ^
-m com.github.gv2011.jctrl.service/com.github.gv2011.jctrl.service.Main ^
RUN9
