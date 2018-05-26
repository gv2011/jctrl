package com.github.gv2011.jctrl.service;

import static com.github.gv2011.jctrl.service.Main.PROCESS;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.ControlSocket.Command;
import com.github.gv2011.jctrl.service.Main.MainCommand;

public class DetachedRunner {

  private static final String VERSION = "0.0.1-SNAPSHOT";
  private static final String SERVICE_DIR = "C:/programs/jctrl";

  private static final Logger LOG = getLogger(DetachedRunner.class);

  public void run() {
    LOG.info("{}: Started.", PROCESS);
    checkControlPortIsAvailable();
    startService();
    new ControlConnection().sendCommand(Command.WAIT_FOR_TERMINATION);
  }


  private void checkControlPortIsAvailable() {
    final int port = Main.CONTROL_PORT;
    callWithCloseable(()->new ServerSocket(), s->{
      try {
        s.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
      } catch (final IOException e) {
        LOG.error("{}: Control port {} is not available.", PROCESS);
        throw new RuntimeException(format("{}: Control port {} is not available.", PROCESS, port), e);
      }
    });
    LOG.info("{}: Control port {} is available.", PROCESS, port);
  }

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
