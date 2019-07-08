package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.CollectionUtils.single;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.bug;
import static com.github.gv2011.util.ex.Exceptions.call;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.JCtrl;
import com.github.gv2011.jctrl.ShutdownSocket.Command;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.main.MainUtils;

public final class Main{

  private static final Logger LOG = getLogger(Main.class);

  static enum MainCommand{RUN, RUN9, STOP, RUN_INTERNAL}

  private static final AtomicBoolean RUNNING = new AtomicBoolean();

  public static void main(final String[] args) {
    verify(RUNNING.compareAndSet(false, true));
    MainUtils.runCommand(
      args,
      c->{
        final Main main = new Main();
        main.dispatch(MainCommand.valueOf(single(args)));
      },
      Nothing.class
    );
  }

  private final ArtifactRef artifact = new JCtrlServiceMarker().artifactRef();
  private final Path installDirectory;
  private final int port = 2997;

  private Main(){
    installDirectory = call(()->Paths.get(".").toRealPath());
  }

  private void dispatch(final MainCommand cmd) {
    LOG.warn("Started with command {} in directory {}.", cmd, installDirectory);
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
