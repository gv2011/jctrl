package com.github.gv2011.jctrl.service;

import static com.github.gv2011.jctrl.service.Main.PROCESS;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.Main.MainCommand;

final class DetachedRunnerCp extends DetachedRunner{

  private static final Logger LOG = LoggerFactory.getLogger(DetachedRunnerCp.class);

  @Override
  void startService() {
    LOG.info("{}: Starting service.", PROCESS);
    final File inFile = new File("in.txt");
    call(inFile::createNewFile);
    final ProcessBuilder command = new ProcessBuilder()
      .directory(new File(SERVICE_DIR))
      .command(
        "cmd", "/c", "start" , "java", "-jar",
        format("jctrl-service-{}-jar-with-dependencies.jar", VERSION),
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
