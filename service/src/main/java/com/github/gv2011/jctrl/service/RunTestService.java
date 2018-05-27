package com.github.gv2011.jctrl.service;

import static com.github.gv2011.jctrl.service.Main.PROCESS;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.github.gv2011.jctrl.service.Main.MainCommand;

public class RunTestService {
  private static final String VERSION = "0.0.1-SNAPSHOT";

  private static final Logger LOG = getLogger(RunTestService.class);

  public static void main(final String[] args) throws InterruptedException {
      LOG.info("Starting.");
      final File inFile = new File("in.txt");
      call(inFile::createNewFile);
      final ProcessBuilder command = new ProcessBuilder()
        .command(
            "java", "-classpath",
            format("target/jctrl-service-{}-jar-with-dependencies.jar", VERSION),
            TestService.class.getName()
        )
        .redirectInput(inFile)
        .redirectOutput(new File("out.txt"))
        .redirectError(new File("err.txt"))
        ;
        LOG.info("Starting service.", PROCESS);
        final Process p = call(command::start);
        Thread.sleep(3000);
        LOG.info("Destroying service.", PROCESS);

        p.destroy();
        if(!p.waitFor(1, TimeUnit.SECONDS)){
          LOG.info("Destroying forcibly.", PROCESS);
          final int s = p.destroyForcibly().waitFor();
        }
        LOG.info("Done.", PROCESS);
  }

}
