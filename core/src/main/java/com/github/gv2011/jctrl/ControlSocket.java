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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Nothing;

public final class ControlSocket{

  private static final Logger LOG = getLogger(ControlSocket.class);

  public static final int CONTROL_PORT = 2997;


  public static enum Command{WAIT_FOR_TERMINATION, STOP;
    public String response(){
      return "DONE:"+name();
    }
  }

    public static ControlSocket create(final AutoCloseableNt controlled, final String processName) {
        boolean success = false;
        final ServerSocket serverSocket = call(()->new ServerSocket());
        try {
            call(()->serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), CONTROL_PORT)));
            final ControlSocket result = new ControlSocket(serverSocket, controlled, processName);
            success = true;
            return result;
        }
        finally {
            if(!success) call(serverSocket::close);
        }
    }

    private final String process;
    private final ExecutorService executors = Executors.newCachedThreadPool();
    private final ServerSocket serverSocket;
    private final AutoCloseableNt controlled;
    private final Future<Nothing> acceptLoop;

    private final Object terminatedLock = new Object();
    private boolean terminated;
    private final Thread shutdown;

    private ControlSocket(final ServerSocket serverSocket, final AutoCloseableNt controlled, final String processName) {
        this.serverSocket = serverSocket;
        this.controlled = controlled;
        process = processName;
        acceptLoop = executors.submit(this::acceptLoop);
        shutdown = new Thread(
            ()->{
                call(serverSocket::close);
                call(()->acceptLoop.get());
                executors.shutdown();
                boolean success = false;
                while(!success) {
                    success = call(()->executors.awaitTermination(5, TimeUnit.SECONDS));
                    if(!success) {
                      LOG.warn("{}: Waiting for executor service shutdown.", process);
                    }
                }
            },
            ControlSocket.class.getSimpleName()+"-close"
        );
    }

    public Nothing waitUntilShutdown(){
      return call(()->shutdown.join());
    }


    private void close() {
      boolean alreadyTerminated;
      synchronized(terminatedLock) {
        alreadyTerminated = terminated;
        terminated = true;
        terminatedLock.notifyAll();
      }
      if(!alreadyTerminated) shutdown.start();
    }

    private Nothing acceptLoop() {
        boolean terminated = false;
        while(!terminated) {
            Optional<Socket> socket = Optional.empty();
            try {
                socket = Optional.of(serverSocket.accept());
            }catch(final IOException ex) {
                synchronized(terminatedLock) {
                    terminated = this.terminated;
                }
                if(!terminated) {
                    LOG.error(format("{}: Accept failed.", process), ex);
                }
            }
            socket.ifPresent(s->executors.submit(()->handleConnection(s)));
        }
        LOG.info("{}: Accept loop finished.", process);
        return Nothing.INSTANCE;
    }

    private Nothing handleConnection(final Socket socket) {
      LOG.info("{}: Connection handling started.", process);
      try{
        return callWithCloseable(()->socket, s->{
            final BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            final Writer out = new OutputStreamWriter(socket.getOutputStream());
            final Optional<Command> command = readCommand(r);
            verify(command.isPresent());
            handleCommand(command.get(), out);
            verifyEqual(readCommand(r), Optional.empty());
            return Nothing.INSTANCE;
        });
      }catch(final Throwable t){
        LOG.error(format("{}: Connection handling aborted because of exception.", process), t);
        return Nothing.INSTANCE;
      }
      finally{
        LOG.info("{}: Connection handling finished.", process);
      }
    }


    private Optional<Command> readCommand(final BufferedReader r) throws IOException {
      return Optional.ofNullable(r.readLine()).map(Command::valueOf);
    }


    private Nothing handleCommand(final Command command, final Writer out) throws Exception {
      LOG.info("{}: Handling command {}.", process, command);
      if(command.equals(Command.WAIT_FOR_TERMINATION)) {
          waitForTermination(out);
      }
      else if(command.equals(Command.STOP)) {
          stop();
      }
      else;
      confirm(command, out);
      LOG.info("{}: Command {} done.", process, command);
      return Nothing.INSTANCE;
    }


    private void confirm(final Command command, final Writer out) throws IOException {
        out.write(command.response()+"\n");
        out.flush();
    }


    private void stop() throws IOException {
        controlled.close();
        close();
    }




    private Nothing waitForTermination(final Writer out) throws Exception {
        boolean terminated = false;
        while(!terminated) {
            out.write("tick\n");
            out.flush();
            synchronized(terminatedLock) {
                terminated = this.terminated;
                if(!terminated) {
                    terminatedLock.wait(1000);
                }
            }
        }
        return Nothing.INSTANCE;
    }
}
