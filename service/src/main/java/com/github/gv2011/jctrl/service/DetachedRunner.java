package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.ShutdownSocket.Command;

abstract class DetachedRunner {

  private static final Logger LOG = getLogger(DetachedRunner.class);

  final Path installDirectory;
  final String command;
  private final int port;

  DetachedRunner(final Path installDirectory, final int port, final String command) {
    this.installDirectory = installDirectory;
    this.port = port;
    this.command = command;
  }


  final void run() {
    LOG.info("Started.");
    checkControlPortIsAvailable();
    startService();
    new ControlConnection(port).sendCommand(Command.WAIT_FOR_TERMINATION);
  }


  private void checkControlPortIsAvailable() {
    final InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    callWithCloseable(()->new ServerSocket(), s->{
      try {
        s.bind(endpoint);
      } catch (final IOException e) {
        throw new RuntimeException(format("Control port at {} is not available.", endpoint), e);
      }
    });
    LOG.info("Control port at {} is available.", port);
  }

  abstract void startService();

}
