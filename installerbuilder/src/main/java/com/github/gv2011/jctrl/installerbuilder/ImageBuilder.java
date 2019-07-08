package com.github.gv2011.jctrl.installerbuilder;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.spi.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.JCtrlServiceMarker;
import com.github.gv2011.jctrl.service.Main;
import com.github.gv2011.m2t.ArtifactId;
import com.github.gv2011.m2t.ArtifactMarker;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.util.UtilModuleMarker;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.tstr.TypedString;

public class ImageBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ImageBuilder.class);

  public static void main(final String[] args) {
    try(ModulePathBuilder mpb = new ModulePathBuilder(M2tFactory.INSTANCE.get().create())){
      new ImageBuilder(mpb).buildImage(TypedString.create(ArtifactId.class, "jctrl-service"));
    }
  }

  private final ModulePathBuilder mpb;

  public ImageBuilder(final ModulePathBuilder mpb) {
    this.mpb = mpb;
  }

  public void buildImage(final ArtifactId artifactId){
    final ArtifactMarker marker = new JCtrlServiceMarker();
    final ArtifactRef artifactRef = marker.artifactRef();
    final IList<Path> allJars = mpb.getAllJars(artifactRef);
    LOG.info("Module Path:\n  {}", allJars.stream().map(Path::toString).collect(joining("\n  ")));
    final String modulePath = toString(allJars);

    final ToolProvider tp = ToolProvider.findFirst("jlink").get();
    final StringWriter swOut = new StringWriter();
    final PrintWriter out = new PrintWriter(swOut);
    final StringWriter swErr = new StringWriter();
    final PrintWriter err = new PrintWriter(swErr);
    tp.run(
      out, err,
      new String[]{
        "--output", "target/jlink-service",
        "--launcher", "main="+marker.module().getName()+"/"+Main.class.getName(),
        "--module-path", modulePath,
        "--add-modules", marker.module().getName(),
        "--add-modules", UtilModuleMarker.class.getModule().getName(),
        "--add-modules", "com.github.gv2011.util.gcol",
        "--add-modules", "com.github.gv2011.util.beans.imp",
        "--add-modules", "com.github.gv2011.jsong",
        "--add-modules", "com.github.gv2011.util.log.logback"
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
