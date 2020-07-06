package com.github.gv2011.jctrl;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.sec.SecUtils;

public final class ShutdownSocket {

  private static final Logger LOG = getLogger(ShutdownSocket.class);

  public static enum Command {WAIT_FOR_TERMINATION, STOP;
    public String response() {
      return "DONE:" + name();
    }
  }

  public static ShutdownSocket create(final int port, final Path certificateDirectory, final AutoCloseableNt controlled) {
    boolean success = false;
    final InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    final SSLServerSocketFactory ssf = SecUtils.createServerSocketFactory(certificateDirectory, true);
    final PublicKey publicKey = SecUtils.getPublicKey(certificateDirectory);
    final SSLServerSocket serverSocket =
      (SSLServerSocket) call(()->ssf.createServerSocket())
    ;
    try {
      serverSocket.setNeedClientAuth(true);
      call(() -> serverSocket.bind(endpoint));
      final ShutdownSocket result = new ShutdownSocket(serverSocket, publicKey, controlled);
      success = true;
      return result;
    } finally {
      if (!success) call(serverSocket::close);
    }
  }

  private final ExecutorService executors;
  private final SSLServerSocket serverSocket;
  private final AutoCloseableNt controlled;
  private final Future<Nothing> acceptLoop;

  private final Object          terminatedLock = new Object();
  private boolean               terminated;
  private final Thread          shutdown;
  private final PublicKey       shutdownAllowed;

  private ShutdownSocket(
    final SSLServerSocket serverSocket, final PublicKey shutdownAllowed, final AutoCloseableNt controlled
  ) {
    this.serverSocket = serverSocket;
    this.shutdownAllowed = shutdownAllowed;
    this.controlled = controlled;
    executors = Executors.newCachedThreadPool();
    acceptLoop = executors.submit(this::acceptLoop);
    shutdown = new Thread(
      () -> {
        call(serverSocket::close);
        call(() -> acceptLoop.get());
        executors.shutdown();
        boolean success = false;
        while (!success) {
          success = call(() -> executors.awaitTermination(5, TimeUnit.SECONDS));
          if (!success) {
            LOG.warn("Waiting for executor service shutdown.");
          }
        }
      },
      ShutdownSocket.class.getSimpleName() + "-close"
    );
  }

  public Nothing waitUntilShutdown() {
    return call(() -> shutdown.join());
  }

  private void close() {
    boolean alreadyTerminated;
    synchronized (terminatedLock) {
      alreadyTerminated = terminated;
      terminated = true;
      terminatedLock.notifyAll();
    }
    if (!alreadyTerminated) shutdown.start();
  }

  private Nothing acceptLoop() {
    boolean terminated = false;
    while (!terminated) {
      Optional<SSLSocket> socket = Optional.empty();
      try {
        socket = Optional.of((SSLSocket)serverSocket.accept());
      } catch (final IOException ex) {
        synchronized (terminatedLock) {
          terminated = this.terminated;
        }
        if (!terminated) {
          LOG.error("Accept failed.", ex);
        }
      }
      socket.ifPresent(s -> executors.submit(() -> handleConnection(s)));
    }
    LOG.info("Accept loop finished.");
    return Nothing.INSTANCE;
  }

  private Nothing handleConnection(final SSLSocket socket) {
    LOG.info("Connection handling started.");
    try {
      return callWithCloseable(() -> socket, s -> {
        verifyEqual(s.getSession().getPeerCertificates()[0].getPublicKey(), shutdownAllowed);
        final BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        final Optional<Command> command = readCommand(r);
        verify(command.isPresent());
        handleCommand(command.get(), out);
        verifyEqual(readCommand(r), Optional.empty());
        return Nothing.INSTANCE;
      });
    } catch (final Throwable t) {
      LOG.error(format("Connection handling aborted because of exception."), t);
      return Nothing.INSTANCE;
    } finally {
      LOG.info("{}: Connection handling finished.");
    }
  }

  private Optional<Command> readCommand(final BufferedReader r) throws IOException {
    return Optional.ofNullable(r.readLine()).map(Command::valueOf);
  }

  private Nothing handleCommand(final Command command, final Writer out) throws Exception {
    LOG.info("Handling command {}.", command);
    if (command.equals(Command.WAIT_FOR_TERMINATION)) {
      waitForTermination(out);
    } else if (command.equals(Command.STOP)) {
      stop();
    }
    confirm(command, out);
    LOG.info("Command {} done.", command);
    return Nothing.INSTANCE;
  }

  private void confirm(final Command command, final Writer out) throws IOException {
    out.write(command.response() + "\n");
    out.flush();
  }

  private void stop(){
    controlled.close();
    close();
  }

  private Nothing waitForTermination(final Writer out) throws Exception {
    boolean terminated = false;
    while (!terminated) {
      out.write("tick\n");
      out.flush();
      synchronized (terminatedLock) {
        terminated = this.terminated;
        if (!terminated) {
          terminatedLock.wait(1000);
        }
      }
    }
    return Nothing.INSTANCE;
  }
}
