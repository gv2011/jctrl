package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.CollectionUtils.single;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.Verify.verifyIn;
import static com.github.gv2011.util.ex.Exceptions.bug;
import static com.github.gv2011.util.icol.ICollections.setOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.ShutdownSocket;
import com.github.gv2011.jctrl.JCtrl;
import com.github.gv2011.jctrl.ShutdownSocket.Command;
import com.github.gv2011.util.Nothing;

import ch.qos.logback.classic.LoggerContext;

public class Main {

  static enum MainCommand{RUN, RUN9, STOP, RUN_INTERNAL}

  static final int CONTROL_PORT = ShutdownSocket.CONTROL_PORT;

  private static final Logger LOG = getLogger(Main.class);

  static final String PROCESS = ManagementFactory.getRuntimeMXBean().getName();

  private static final AtomicBoolean RUNNING = new AtomicBoolean();

  public static void main(final String[] args) {
    int returnCode = 17;
    try {
      verify(RUNNING.compareAndSet(false, true));
      final MainCommand cmd = MainCommand.valueOf(single(args));
      LOG.warn("{}: Started with command {}.", PROCESS, cmd);
      if(cmd.equals(MainCommand.RUN)) runInternalCp();
      else if(cmd.equals(MainCommand.RUN9)) runInternalMp();
      else if(cmd.equals(MainCommand.STOP)) stopInternal();
      else if(cmd.equals(MainCommand.RUN_INTERNAL)) {
        runTarget();
      }
      else bug();
      returnCode = 0;
    }
    catch (final Throwable t) {
      LOG.error(PROCESS, t);
    }
    finally {
        LOG.warn("{}: main terminated.", PROCESS);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
        if(returnCode!=0) System.exit(returnCode);
    }
   }

  @SuppressWarnings("resource")
  private static void runTarget() {
    new JCtrl(PROCESS).run();
  }

  private static void runInternalCp() {
    new DetachedRunnerCp().run();
  }

  private static void runInternalMp() {
    new DetachedRunnerMp().run();
  }

  public static void stop(final String[] args) {
    try {
      LOG.warn("{}: Started stop.", PROCESS);
      stopInternal();
    }
    catch (final Throwable t) {
      LOG.error(PROCESS, t);
    }
    finally {
      LOG.warn("{}: stop terminated.", PROCESS);
    }
  }

  private static Nothing stopInternal() {
    return new ControlConnection().sendCommand(Command.STOP);
  }

  public static void run(final String[] args) {
    try {
      verifyEqual(args.length, 1);
      verifyIn(args[0], setOf(MainCommand.RUN.toString(), MainCommand.RUN9.toString()), (e, a)->a.toString());
      verify(RUNNING.compareAndSet(false, true));
      LOG.warn("{}: Started run.", PROCESS);
      if(args[0].equals(MainCommand.RUN.toString())) runInternalCp();
      else if(args[0].equals(MainCommand.RUN9.toString())) runInternalCp();
      else bug();
    }
    catch (final Throwable t) {
      LOG.error(PROCESS, t);
    }
    finally {
        LOG.warn("{}: run terminated.", PROCESS);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
   }
}
