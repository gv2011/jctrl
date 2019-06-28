package com.github.gv2011.jctrl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Simple {

  private static final Path ROOT = FileSystems.getDefault().getRootDirectories().iterator().next();
  private static final UUID ID = UUID.randomUUID();
  private static final CountDownLatch LATCH1 = new CountDownLatch(1);
  private static final CountDownLatch LATCH2 = new CountDownLatch(1);

  public static void main(final String[] args) throws InterruptedException {
    log("started");
    final Thread hook = new Thread(Simple::shutdown, "shutdown");
    Runtime.getRuntime().addShutdownHook(hook);
    log("waiting");
    LATCH1.await();
    log("finished");
    LATCH2.countDown();
  }

  private static void log(final String msg) {
    try {
      Files.writeString(
          ROOT.resolve("jctrl/jctrl.log"),
          ID+" "+Instant.now()+" "+msg+"\n", UTF_8,
          StandardOpenOption.APPEND, StandardOpenOption.CREATE
        );
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private static void shutdown() {
    log("shutdown");
    LATCH1.countDown();
    try {
      LATCH2.await();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

}
