package com.github.gv2011.jctrl.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SomeService {

    private static final CountDownLatch STOP = new CountDownLatch(1);

    public static void run(final String[] args) {
      try{
        System.out.println("run");
        while(!STOP.await(5, TimeUnit.SECONDS)) System.out.println("running");
      }
      catch(final Throwable t){t.printStackTrace();}
      finally{System.out.println("stopped");}
    }

    public static void stop(final String[] args) {
        System.out.println("stop");
        STOP.countDown();
    }

    public static void start(final String[] args) {
        System.out.println("start");
    }


}