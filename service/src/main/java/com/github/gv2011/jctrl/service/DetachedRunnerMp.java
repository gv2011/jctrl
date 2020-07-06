package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DetachedRunnerMp extends DetachedRunner{

  private static final Logger LOG = LoggerFactory.getLogger(DetachedRunnerMp.class);

  private final Path java;
  private final Class<?> mainClass;

  DetachedRunnerMp(final Path installDirectory, final Path java, final int port, final Class<?> mainClass, final String command) {
    super(installDirectory, port, command);
//    java = installDirectory.resolve("bin").resolve("java.exe");
    verify(Files.isRegularFile(java));
    this.java = java;
    this.mainClass = mainClass;
  }

  @Override
  void startService() {
    LOG.info("Starting {} {} in new detached process.", mainClass.getName(), command);
    final File inFile = new File("in.txt");
    call(inFile::createNewFile);
    final ProcessBuilder processBuilder = new ProcessBuilder()
      .directory(installDirectory.toFile())
      .command(
        "cmd", "/c", "start" , java.toString(),
        format("-DLOG_PREFIX={}-", command),
        "-m",
        format("{}/{}", mainClass.getModule().getName(), mainClass.getName()),
        //e.g.: com.github.gv2011.jctrl.simple/com.github.gv2011.jctrl.simple.Simple
        command
      )
      .redirectInput(inFile)
      .redirectOutput(new File("out.txt"))
      .redirectError(new File("err.txt"))
    ;
    call(processBuilder::start);
  }
}
