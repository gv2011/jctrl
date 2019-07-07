package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.ex.Exceptions.call;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.ShutdownSocket;

public class DetachedRunnerMTst {

  private static final Logger LOG = getLogger(DetachedRunnerMTst.class);

  private final CountDownLatch latch = new CountDownLatch(1);

//  @Test
  public void testRun() throws Exception {
    final AtomicReference<ShutdownSocket> controlSocket = new AtomicReference<>();
    final Path dir = Paths.get(".").toRealPath();
    final int port = 2997;
//    final Thread detached = new Thread(()->{
//      LOG.info("Starting DetachedRunner.");
//      new DetachedRunnerMp(dir, port){
//        @Override
//        void startService() {
//          LOG.info("Starting service.");
//          controlSocket.set(ShutdownSocket.create(()->stop(), "test"));
//          LOG.info("Service started.");
//        }
//      }.run();
//      LOG.info("DetachedRunner terminated.");
//    });
//    detached.start();
    call(()->Thread.sleep(1500));
    LOG.info("Sending STOP.");
//    new ControlConnection().sendCommand(Command.STOP);
    latch.await();
    LOG.info("Stopped, waiting for detached.");
//    detached.join();
    LOG.info("Joined detached, waiting for ControlSocket.");
    controlSocket.get().waitUntilShutdown();
    LOG.info("Test terminated.");
  }

  private void stop(){
    LOG.info("Stopping.");
    latch.countDown();
  }

}
