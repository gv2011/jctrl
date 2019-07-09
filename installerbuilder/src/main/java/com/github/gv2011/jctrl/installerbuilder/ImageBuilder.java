package com.github.gv2011.jctrl.installerbuilder;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.ex.Exceptions.format;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.spi.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.jctrl.service.JCtrlServiceMarker;
import com.github.gv2011.jctrl.service.Main;
import com.github.gv2011.m2t.ArtifactMarker;
import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.UtilModuleMarker;
import com.github.gv2011.util.icol.ISortedSet;
import com.github.gv2011.util.main.MainUtils;

public class ImageBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ImageBuilder.class);

  public static void main(final String[] args) {
    MainUtils.runCommand(
      args,
      n->{
        try(M2t m2t = M2tFactory.INSTANCE.get().create()){
          final ModulePathBuilder mpb = new ModulePathBuilder(m2t);
          final PrunsrvLoader prunsrvLoader = new PrunsrvLoader(m2t);
          new ImageBuilder(mpb, prunsrvLoader).buildImage();
        }
      },
      Nothing.class
    );
  }

  private final ModulePathBuilder mpb;
  private final PrunsrvLoader prunsrvLoader;
  private final Path imageDirectory;

  public ImageBuilder(final ModulePathBuilder mpb, final PrunsrvLoader prunsrvLoader) {
    this.mpb = mpb;
    this.prunsrvLoader = prunsrvLoader;
    imageDirectory = Paths.get("image").toAbsolutePath().normalize();
  }

  public Path imageDirectory(){
    return imageDirectory;
  }

  public void buildImage(){
    final ArtifactMarker marker = new JCtrlServiceMarker();
    final ArtifactRef artifactRef = marker.artifactRef();
    final ISortedSet<Path> allJars = mpb.getAllJars(artifactRef);
    LOG.info("Module Path:\n  {}", allJars.stream().map(Path::toString).collect(joining("\n  ")));
    final String modulePath = toString(allJars);

    FileUtils.delete(imageDirectory.toFile());

    final ToolProvider tp = ToolProvider.findFirst("jlink").get();
    final StringWriter swOut = new StringWriter();
    final PrintWriter out = new PrintWriter(swOut);
    final StringWriter swErr = new StringWriter();
    final PrintWriter err = new PrintWriter(swErr);
    final int resultCode = tp.run(
      out, err,
      new String[]{
        "--output", imageDirectory.toString(),
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
    verifyEqual(resultCode, 0);

    prunsrvLoader.loadPrunsrv(imageDirectory.resolve("bin"));

    copyResources("jctrl.ico", "jctrl.ico.source.txt");
  }

  private void copyResources(final String... resouceNames){
    Arrays.stream(resouceNames).forEach(
      r->FileUtils.copy(getClass().getResource(r), imageDirectory.resolve(r))
    );
  }

  private String toString(final ISortedSet<Path> allJars) {
    final String pathSep = Character.toString(File.pathSeparatorChar);
    return allJars.stream().map(Path::toString).collect(joining(pathSep));
  }


}
