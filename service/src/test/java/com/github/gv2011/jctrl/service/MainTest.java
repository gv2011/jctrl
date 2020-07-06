package com.github.gv2011.jctrl.service;

import org.junit.Test;

public class MainTest {

  static{
    System.setProperty("LOG_PREFIX", "T");
  }

  @Test
  public void test() {
    Main.main(new String[]{"RUN9"});
  }

}
