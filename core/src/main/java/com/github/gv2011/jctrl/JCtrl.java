package com.github.gv2011.jctrl;

import static com.github.gv2011.util.Nothing.nothing;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.util.concurrent.CountDownLatch;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Nothing;

public class JCtrl implements AutoCloseableNt{

  private final CountDownLatch latch = new CountDownLatch(1);
  private final String processName;


  public JCtrl(final String processName) {
    this.processName = processName;
  }

  public Nothing run(){
    final ShutdownSocket controlSocket = ShutdownSocket.create(()->close(), processName);
    call(()->latch.await());
    controlSocket.waitUntilShutdown();
    return nothing();
  }

  public void close() {
    latch.countDown();
  }

}
