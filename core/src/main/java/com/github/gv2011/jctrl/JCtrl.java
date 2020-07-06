package com.github.gv2011.jctrl;

import static com.github.gv2011.util.Nothing.nothing;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Path;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.lock.Latch;

public class JCtrl implements AutoCloseableNt{

  private final Latch<Nothing> latch = Latch.create();
  private final int port;
  private final Path certificateDirectory;

  public JCtrl(final int port, final Path certificateDirectory) {
    this.port = port;
    this.certificateDirectory = certificateDirectory;
  }

  public Nothing run(){
    final ShutdownSocket controlSocket = ShutdownSocket.create(port, certificateDirectory, ()->close());
    call(()->latch.await());
    controlSocket.waitUntilShutdown();
    return nothing();
  }

  public void close() {
    latch.release(nothing());
  }

}
