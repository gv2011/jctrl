package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.Main.MainCommand;
import com.github.gv2011.m2t.ArtifactRef;

final class DetachedRunnerCp extends DetachedRunner{

  private static final Logger LOG = LoggerFactory.getLogger(DetachedRunnerCp.class);

  private final ArtifactRef artifact;

  DetachedRunnerCp(final Path installDirectory, final int port, final ArtifactRef artifact, final String command) {
    super(installDirectory, port, command);
    this.artifact = artifact;
  }

  @Override
  void startService() {
    LOG.info("Starting service.");
    final File inFile = new File("in.txt");
    call(inFile::createNewFile);
    final ProcessBuilder processBuilder = new ProcessBuilder()
      .directory(installDirectory.toFile())
      .command(
        "cmd", "/c", "start" , "java",
        format("-DLOG_PREFIX={}-", command),
        "-jar",
        format("{}-{}-jar-with-dependencies.jar", artifact.artifactId(), artifact.version()),
        MainCommand.RUN_INTERNAL.name()
      )
      .redirectInput(inFile)
      .redirectOutput(new File("out.txt"))
      .redirectError(new File("err.txt"))
    ;
    LOG.info("Starting detached controller process.");
    call(processBuilder::start);
  }


}
