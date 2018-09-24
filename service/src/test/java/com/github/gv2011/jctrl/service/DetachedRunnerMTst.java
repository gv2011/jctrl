package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.ex.Exceptions.call;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;

import com.github.gv2011.jctrl.ShutdownSocket;
import com.github.gv2011.jctrl.ShutdownSocket.Command;
import com.github.gv2011.jctrl.service.ControlConnection;
import com.github.gv2011.jctrl.service.DetachedRunner;

public class DetachedRunnerMTst {

  private static final Logger LOG = getLogger(DetachedRunnerMTst.class);

  private final CountDownLatch latch = new CountDownLatch(1);

  @Test
  public void testRun() throws InterruptedException {
    final AtomicReference<ShutdownSocket> controlSocket = new AtomicReference<>();
    final Thread detached = new Thread(()->{
      LOG.info("Starting DetachedRunner.");
      new DetachedRunner(){
        @Override
        void startService() {
          LOG.info("Starting service.");
          controlSocket.set(ShutdownSocket.create(()->stop(), "test"));
          LOG.info("Service started.");
        }
      }.run();
      LOG.info("DetachedRunner terminated.");
    });
    detached.start();
    call(()->Thread.sleep(1500));
    LOG.info("Sending STOP.");
    new ControlConnection().sendCommand(Command.STOP);
    latch.await();
    LOG.info("Stopped, waiting for detached.");
    detached.join();
    LOG.info("Joined detached, waiting for ControlSocket.");
    controlSocket.get().waitUntilShutdown();
    LOG.info("Test terminated.");
  }

  private void stop(){
    LOG.info("Stopping.");
    latch.countDown();
  }

}
