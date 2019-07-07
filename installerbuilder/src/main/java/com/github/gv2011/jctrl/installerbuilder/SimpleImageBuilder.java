package com.github.gv2011.jctrl.installerbuilder;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.spi.ToolProvider;

import com.github.gv2011.jctrl.simple.Simple;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.UtilModuleMarker;
import com.github.gv2011.util.icol.IList;

public class SimpleImageBuilder {

  public static void main(final String[] args) {
    try(ModulePathBuilder mpb = new ModulePathBuilder(M2tFactory.INSTANCE.get().create())){
      new SimpleImageBuilder(mpb).buildImage();
    }
  }

  private final ModulePathBuilder mpb;

  public SimpleImageBuilder(final ModulePathBuilder mpb) {
    this.mpb = mpb;
  }

  public void buildImage(){
    final ArtifactRef own = new ArtifactRefReader().readArtifactRef();
    final ArtifactRef artifactRef = BeanUtils.beanBuilder(ArtifactRef.class)
      .set    (ArtifactRef::groupId   ).to(own.groupId())
      .setTStr(ArtifactRef::artifactId).to("jctrl-simple")
      .set    (ArtifactRef::version   ).to(own.version())
      .build()
    ;
    final String modulePath = toString(mpb.getAllJars(artifactRef));

    final ToolProvider tp = ToolProvider.findFirst("jlink").get();
    final StringWriter swOut = new StringWriter();
    final PrintWriter out = new PrintWriter(swOut);
    final StringWriter swErr = new StringWriter();
    final PrintWriter err = new PrintWriter(swErr);
    tp.run(
      out, err,
      new String[]{
        "--output", "target/jlink-simple",
        "--launcher", "main="+Simple.class.getModule().getName()+"/"+Simple.class.getName(),
        "--module-path", modulePath,
        "--add-modules", Simple.class.getModule().getName(),
        "--add-modules", UtilModuleMarker.class.getModule().getName(),
        "--add-modules", "com.github.gv2011.util.gcol",
        "--add-modules", "ch.qos.logback.classic"
      }
    );
    out.flush();
    err.flush();
    System.out.println("Out:\n"+swOut.toString());
    System.out.println("Err:\n"+swErr.toString());
  }



  private String toString(final IList<Path> allJars) {
    final String pathSep = Character.toString(File.pathSeparatorChar);
    return allJars.stream().map(Path::toString).collect(joining(pathSep));
  }


}
