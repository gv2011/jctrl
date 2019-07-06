package com.github.gv2011.jctrl.simple;

import java.time.Duration;

import org.junit.Test;

import com.github.gv2011.util.time.Clock;


public class SimpleTest {

  @Test
  public void test() {
    new Thread(this::stop).start();
    Simple.start(new String[0]);
  }

  private void stop(){
    Clock.get().sleep(Duration.ofSeconds(3));
    Simple.stop(new String[0]);
  }

}
