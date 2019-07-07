package com.github.gv2011.jctrl;

import static com.github.gv2011.util.Nothing.nothing;
import static com.github.gv2011.util.ex.Exceptions.call;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.lock.Latch;

public class JCtrl implements AutoCloseableNt{

  private final Latch<Nothing> latch = Latch.create();
  private final int port;

  public JCtrl(final int port) {
    this.port = port;
  }

  public Nothing run(){
    final ShutdownSocket controlSocket = ShutdownSocket.create(port, ()->close());
    call(()->latch.await());
    controlSocket.waitUntilShutdown();
    return nothing();
  }

  public void close() {
    latch.release(nothing());
  }

}
