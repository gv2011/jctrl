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

  static final String CERT_DIR_NAME = "tls";

  static enum MainCommand{RUN, RUN9, STOP, RUN_INTERNAL}

  private static final AtomicBoolean RUNNING = new AtomicBoolean();

  public static void main(final String[] args) {
     main2(args, null);
  }

  static void main2(final String[] args, final Path java) {
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
  private final Path certificateDirectory;
  private Path java;
  private final int port = 2997;


  private Main(){
    installDirectory = call(()->Paths.get(".").toRealPath());
    certificateDirectory = installDirectory.resolve(CERT_DIR_NAME);
  }

  private Main(final Path installDirectory, final Path java){
    this.installDirectory = installDirectory;
    certificateDirectory = installDirectory.resolve(CERT_DIR_NAME);
    this.java = java;
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
    new JCtrl(port, certificateDirectory).run();
  }

  private void runInternalCp() {
    new DetachedRunnerCp(installDirectory, port, artifact, MainCommand.RUN_INTERNAL.name()).run();
  }

  private void runInternalMp() {
    new DetachedRunnerMp(installDirectory, java, port, Main.class, MainCommand.RUN_INTERNAL.name()).run();
  }

  private Nothing stopInternal() {
    return new ControlConnection(port, certificateDirectory).sendCommand(Command.STOP);
  }

}
