package com.github.gv2011.jctrl.installerbuilder;

import java.io.PrintWriter;
import com.github.gv2011.jctrl.installerbuilder.ModulePathBuilder;
import java.io.StringWriter;
import java.util.spi.ToolProvider;

import com.github.gv2011.m2t.Version;

public class BuildSimpleImageBuilder {

  public static void main(final String[] args) {
    final ToolProvider tp = ToolProvider.findFirst("jlink").get();
    final StringWriter swOut = new StringWriter();
    final PrintWriter out = new PrintWriter(swOut);
    final StringWriter swErr = new StringWriter();
    final PrintWriter err = new PrintWriter(swErr);
    tp.run(
      out, err,
      new String[]{

      }
    );
    out.flush();
    err.flush();
    System.out.println("Out:\n"+swOut.toString());
    System.out.println("Err:\n"+swErr.toString());
  }

  private ModulePathBuilder mpb;



  public void buildImage(final Version version){

  }

}
