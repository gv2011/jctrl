package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.CollectionUtils.single;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.ex.Exceptions.bug;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.ControlSocket;
import com.github.gv2011.jctrl.JCtrl;
import com.github.gv2011.jctrl.ControlSocket.Command;
import com.github.gv2011.util.Nothing;

import ch.qos.logback.classic.LoggerContext;

public class Main {

  static enum MainCommand{RUN, STOP, RUN_INTERNAL}

  static final int CONTROL_PORT = ControlSocket.CONTROL_PORT;

  private static final Logger LOG = getLogger(Main.class);

  static final String PROCESS = ManagementFactory.getRuntimeMXBean().getName();

  private static final AtomicBoolean RUNNING = new AtomicBoolean();

  public static void main(final String[] args) {
    int returnCode = 1;
    try {
      verifyEqual(RUNNING.getAndSet(true), false);
      final MainCommand cmd = MainCommand.valueOf(single(args));
      LOG.warn("{}: Started with command {}.", PROCESS, cmd);
      if(cmd.equals(MainCommand.RUN)) runInternal();
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

  private static void runTarget() {
    new JCtrl(PROCESS).run();
  }

  private static void runInternal() {
    new DetachedRunner().run();
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
      verifyEqual(RUNNING.getAndSet(true), false);
      LOG.warn("{}: Started run.", PROCESS);
      runInternal();
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
