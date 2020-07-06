package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.Verify.notNull;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.ShutdownSocket.Command;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.sec.SecUtils;
import com.github.gv2011.util.time.Clock;

public class ControlConnection {

  private static final Logger LOG = getLogger(ControlConnection.class);

  private final int port;
  private Path certificateDirectory;
  private final Clock clock = Clock.get();
  private final Duration retryInterval = Duration.ofSeconds(1);


  public ControlConnection(final int port, final Path certificateDirectory){
    this.port = port;
  }

  public Nothing sendCommand(final Command command){
    final boolean retry = command.equals(Command.WAIT_FOR_TERMINATION);
    return callWithCloseable(
      ()->establishControlConnection(retry),
      s->{sendCommand(command, s);}
    );
  }

  private Socket establishControlConnection(final boolean retry) {
    final RSAPublicKey publicKey = SecUtils.getPublicKey(certificateDirectory);
    final InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    Opt<Socket> socket = Opt.empty();
    while(!socket.isPresent()) {
      Opt<Socket> s = Opt.empty();
      try {
        s = Opt.of(SecUtils.connect(certificateDirectory, publicKey, endpoint));
        socket = s;
      } catch (final Exception e) {
        if(retry) LOG.warn(format("Could not connect to control port {}.", port), e);
        else throw wrap(e);
      }
      finally {
        if(!socket.isPresent()) {
          s.ifPresent(s1->call(s1::close));
          if(retry){
            LOG.info("Waiting {} second before trying again.", retryInterval);
            clock.sleep(retryInterval);
          }
        }
      }
    }
    LOG.info("Connected to control port at {}.", endpoint);
    return socket.get();
  }

  private void sendCommand(final Command command, final Socket socket) throws IOException {
    final OutputStream out = socket.getOutputStream();
    out.write((command+"\n").getBytes(UTF_8));
    out.flush();
    final BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
    String response = notNull(r.readLine());
    final String expectedResponse = command.response();
    while(!response.equals(expectedResponse)) {
        LOG.debug("Received {} for command {}, waiting for {}.", response, command, expectedResponse);
        response = notNull(r.readLine());
    }
    LOG.info("Received {} for command {}. Shutting down connection.", response, command);
    socket.shutdownOutput();
    socket.shutdownInput();
  }

}
