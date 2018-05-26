package com.github.gv2011.jctrl;

import static com.github.gv2011.util.Nothing.nothing;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.util.concurrent.CountDownLatch;

import com.github.gv2011.util.Nothing;

public class JCtrl {

  private final CountDownLatch latch = new CountDownLatch(1);
  private final String processName;


  public JCtrl(final String processName) {
    this.processName = processName;
  }

  public Nothing run(){
    final ControlSocket controlSocket = ControlSocket.create(()->stop(), processName);
    call(()->latch.await());
    controlSocket.waitUntilShutdown();
    return nothing();
  }

  private Nothing stop() {
    latch.countDown();
    return nothing();
  }

}
