package com.github.gv2011.jctrl.service;

import static com.github.gv2011.jctrl.service.Main.PROCESS;
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
import java.util.Optional;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.ControlSocket.Command;
import com.github.gv2011.util.Nothing;

public class ControlConnection {

    private static final Logger LOG = getLogger(ControlConnection.class);

    private final int port = Main.CONTROL_PORT;

    public Nothing sendCommand(final Command command){
      final boolean retry = command.equals(Command.WAIT_FOR_TERMINATION);
      return callWithCloseable(
        ()->establishControlConnection(retry),
        s->{sendCommand(command, s);}
      );
    }


    @SuppressWarnings("resource")
    private Socket establishControlConnection(final boolean retry) {
      Optional<Socket> socket = Optional.empty();
      while(!socket.isPresent()) {
        final Socket s = new Socket();
        try {
            LOG.info("{}: Connecting to control port {}.", PROCESS, port);
            s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
            socket = Optional.of(s);
        } catch (final IOException e) {
          if(retry) LOG.warn(format("{}: Could not connect to control port {}.", PROCESS, port), e);
          else throw wrap(e);
        }
        finally {
            if(!socket.isPresent()) {
                call(s::close);
                if(retry){
                  LOG.info("{}: Waiting 1 second before trying again.", PROCESS);
                  call(()->Thread.sleep(1000));
                }
            }
        }
      }
      return socket.get();
    }

    private void sendCommand(final Command command, final Socket socket) throws IOException {
      final OutputStream out = socket.getOutputStream();
      out.write((command+"\n").getBytes(UTF_8));
      out.flush();
      final BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
      String response = notNull(r.readLine());
      while(!response.equals(command.response())) {
          LOG.debug("{}: Received: {}.", PROCESS, response);
          response = notNull(r.readLine());
      }
      LOG.info("{}: Received: {}. Closing output.", PROCESS, response);
      out.close();
    }

}
