package com.github.gv2011.jctrl.service;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestService {

    private static final CountDownLatch STOP = new CountDownLatch(1);

    public static final void main(final String[] args){
      final Thread main = Thread.currentThread();
      Runtime.getRuntime().addShutdownHook(new Thread(
        ()->{
          STOP.countDown();
          call(()->main.join());
        },
        "shutdown"
      ));
      run(args);
    }

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