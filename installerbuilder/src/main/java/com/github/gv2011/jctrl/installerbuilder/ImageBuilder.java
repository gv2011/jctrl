package com.github.gv2011.jctrl.installerbuilder;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.spi.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.JCtrlServiceMarker;
import com.github.gv2011.jctrl.service.Main;
import com.github.gv2011.m2t.ArtifactId;
import com.github.gv2011.m2t.ArtifactMarker;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.UtilModuleMarker;
import com.github.gv2011.util.icol.ISortedSet;
import com.github.gv2011.util.tstr.TypedString;

public class ImageBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ImageBuilder.class);

  public static void main(final String[] args) {
    try(M2t m2t = M2tFactory.INSTANCE.get().create()){
      final ModulePathBuilder mpb = new ModulePathBuilder(m2t);
      final PrunsrvLoader prunsrvLoader = new PrunsrvLoader(m2t);
      new ImageBuilder(mpb, prunsrvLoader).buildImage(TypedString.create(ArtifactId.class, "jctrl-service"));
    }
  }

  private final ModulePathBuilder mpb;
  private final PrunsrvLoader prunsrvLoader;

  public ImageBuilder(final ModulePathBuilder mpb, final PrunsrvLoader prunsrvLoader) {
    this.mpb = mpb;
    this.prunsrvLoader = prunsrvLoader;
  }

  public void buildImage(final ArtifactId artifactId){
    final ArtifactMarker marker = new JCtrlServiceMarker();
    final ArtifactRef artifactRef = marker.artifactRef();
    final ISortedSet<Path> allJars = mpb.getAllJars(artifactRef);
    LOG.info("Module Path:\n  {}", allJars.stream().map(Path::toString).collect(joining("\n  ")));
    final String modulePath = toString(allJars);

    final Path imageDir = Paths.get("target", "image").toAbsolutePath().normalize();
    call(()->Files.createDirectories(imageDir));
    FileUtils.deleteContents(imageDir);

    final ToolProvider tp = ToolProvider.findFirst("jlink").get();
    final StringWriter swOut = new StringWriter();
    final PrintWriter out = new PrintWriter(swOut);
    final StringWriter swErr = new StringWriter();
    final PrintWriter err = new PrintWriter(swErr);
    tp.run(
      out, err,
      new String[]{
        "--output", imageDir.toString(),
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
    LOG.info("Jlink output:\n{}", swOut.toString());
    verify(swErr.toString(), String::isBlank, e->format("Jlink error output:\n{}", e));

    prunsrvLoader.loadPrunsrv(imageDir);

  }



  private String toString(final ISortedSet<Path> allJars) {
    final String pathSep = Character.toString(File.pathSeparatorChar);
    return allJars.stream().map(Path::toString).collect(joining(pathSep));
  }


}
