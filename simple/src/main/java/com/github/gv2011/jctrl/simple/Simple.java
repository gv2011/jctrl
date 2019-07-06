package com.github.gv2011.jctrl.simple;

import static com.github.gv2011.util.Verify.verifyEqual;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;

import com.github.gv2011.util.lock.Lock;

public class Simple {

  private static final Logger LOG = getLogger(Simple.class);

  private static final UUID ID = UUID.randomUUID();

  private static final Lock LOCK = Lock.create();

  private static boolean shouldRun;


  public static void main(final String[] args){
    LOG.info("Main called ({}).", ID);
  }


  public static void start(final String[] args){
    LOG.debug("Start entered ({}).", ID);
    try{
      LOCK.run(()->{
        verifyEqual(shouldRun, false);
        shouldRun = true;
        LOG.info("Started.");
        while(shouldRun){
          LOCK.await(Duration.ofSeconds(1));
          if(shouldRun)LOG.info("Running ({}).", ID);
        }
      });
    }
    finally{
      LOG.info("Terminated ({}).", ID);
      LOG.debug("Start left ({}).", ID);
    }
  }

  public static void stop(final String[] args){
    LOG.debug("Stop entered ({}).", ID);
    try{
      LOCK.run(
        ()->{
          verifyEqual(shouldRun, true);
          shouldRun=false;
        },
        true
      );
    }
    finally{
      LOG.debug("Stop left ({}).", ID);
    }
  }

}
