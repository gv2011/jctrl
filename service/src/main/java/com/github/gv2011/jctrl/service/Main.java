package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.CollectionUtils.single;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.bug;
import static com.github.gv2011.util.ex.Exceptions.call;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.JCtrl;
import com.github.gv2011.jctrl.ShutdownSocket.Command;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.util.Nothing;

import ch.qos.logback.classic.LoggerContext;

public final class Main {

  private static final Logger LOG = getLogger(Main.class);

  static enum MainCommand{RUN, RUN9, STOP, RUN_INTERNAL}

  private static final AtomicBoolean RUNNING = new AtomicBoolean();

  public static void main(final String[] args) {
    int returnCode = 17;
    String processName = UUID.randomUUID().toString();
    try {
      processName = ManagementFactory.getRuntimeMXBean().getName();
      verify(RUNNING.compareAndSet(false, true));
      new Main(processName).dispatch(MainCommand.valueOf(single(args)));
      returnCode = 0;
    }
    catch (final Throwable t) {
      LOG.error("Failed.", t);
    }
    finally {
      LOG.warn("Process {} terminating.", processName);
      final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      loggerContext.stop();
      if(returnCode!=0) System.exit(returnCode);
    }
  }

  private final ArtifactRef artifact = new JCtrlServiceMarker().artifactRef();
  private final String processName;
  private final Path installDirectory;
  private final int port = 2997;

  private Main(final String processName){
    this.processName = processName;
    installDirectory = call(()->Paths.get(".").toRealPath());
  }

  private void dispatch(final MainCommand cmd) {
    LOG.warn("Process {} started with command {} in directory {}.", processName, cmd, installDirectory);
    if(cmd.equals(MainCommand.RUN)) runInternalCp();
    else if(cmd.equals(MainCommand.RUN9)) runInternalMp();
    else if(cmd.equals(MainCommand.STOP)) stopInternal();
    else if(cmd.equals(MainCommand.RUN_INTERNAL)) runTarget();
    else bug();
  }

  @SuppressWarnings("resource")
  private void runTarget() {
    new JCtrl(port).run();
  }

  private void runInternalCp() {
    new DetachedRunnerCp(installDirectory, port, artifact, MainCommand.RUN_INTERNAL.name()).run();
  }

  private void runInternalMp() {
    new DetachedRunnerMp(installDirectory, port, Main.class, MainCommand.RUN_INTERNAL.name()).run();
  }

  private Nothing stopInternal() {
    return new ControlConnection(port).sendCommand(Command.STOP);
  }

}
