package com.github.gv2011.jctrl.service;

import static com.github.gv2011.jctrl.service.Main.PROCESS;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.Main.MainCommand;

final class DetachedRunnerMp extends DetachedRunner{

  private static final Logger LOG = LoggerFactory.getLogger(DetachedRunnerMp.class);

  private static final String JAVA = SERVICE_DIR+"/bin/java.exe";

  @Override
  void startService() {
    LOG.info("{}: Starting service.", PROCESS);
    final File inFile = new File("in.txt");
    call(inFile::createNewFile);
    final ProcessBuilder command = new ProcessBuilder()
      .directory(new File(SERVICE_DIR))
      .command(
        "cmd", "/c", "start" , JAVA, "-m", //com.github.gv2011.jctrl.simple/com.github.gv2011.jctrl.simple.Simple
        format("{}/{}", Main.class.getModule().getName(), Main.class.getName()),
        MainCommand.RUN_INTERNAL.name()
      )
      .redirectInput(inFile)
      .redirectOutput(new File("out.txt"))
      .redirectError(new File("err.txt"))
    ;
    LOG.info("{}: Starting detached controller process.", PROCESS);
    call(command::start);
  }
}
