package com.github.gv2011.jctrl.installerbuilder;

import static com.github.gv2011.util.CollectionUtils.recursiveStream;
import static com.github.gv2011.util.ex.Exceptions.call;
import static java.util.stream.Collectors.joining;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.gv2011.jctrl.service.JCtrlServiceMarker;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.Nothing;
import com.github.gv2011.util.StreamUtils;
import com.github.gv2011.util.XStream;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.main.MainUtils;

public class BuildInnoSetupDirectory {

  public static void main(final String[] args) {
    MainUtils.runCommand(
      args,
      n->{
        try(M2t m2t = M2tFactory.INSTANCE.get().create()){
          final ModulePathBuilder mpb = new ModulePathBuilder(m2t);
          final PrunsrvLoader prunsrvLoader = new PrunsrvLoader(m2t);
          ImageBuilder imageBuilder = new ImageBuilder(mpb, prunsrvLoader);
          BuildInnoSetupDirectory innoSetupDirectoryBuilder = new BuildInnoSetupDirectory(imageBuilder);
          innoSetupDirectoryBuilder.buildInnoSetupDirectory();
        }
      },
      Nothing.class
    );
  }

  private final ImageBuilder imageBuilder;

  BuildInnoSetupDirectory(final ImageBuilder imageBuilder) {
    this.imageBuilder = imageBuilder;
  }

  private void buildInnoSetupDirectory(){
    imageBuilder.buildImage();
    createInnoScript();
  }

  private void createInnoScript() {
    final String template = StreamUtils.readText(()->getClass().getResourceAsStream("jctrl.iss.txt"));
    FileUtils.writeText(
      ( template
        .replace("§§§-VERSION-§§§", new JCtrlServiceMarker().artifactRef().version().toString())
        .replace("§§§-FILES-§§§"  , createFileList())
      ),
      imageBuilder.imageDirectory().resolve("jctrl.iss")
    );
  }

  private String createFileList() {
    final Path dir = imageBuilder.imageDirectory();
    return call(()->Files.list(dir))
      .flatMap(f->{
        return (XStream<Path>)(
          Files.isDirectory(f)
          ? recursiveStream(f, c->call(()->Files.list(c)).filter(c2->Files.isDirectory(c2)))
          : XStream.of(f)
        );
      })
      .filter(this::isFileOrHasFileChildren)
      .map(f->
        Files.isDirectory(f)
        ? innoEntryDir(dir.relativize(f))
        : innoEntryFile(dir, f)
      )
      .collect(joining("\n"))
    ;
  }

  private final boolean isFileOrHasFileChildren(final Path p){
    return Files.isDirectory(p) ? call(()->Files.list(p)).anyMatch(c->!Files.isDirectory(c)) : true;
  }


  private final String winFormat(final Path p){
    return Opt.ofNullable(p.getParent()).map(pa->winFormat(pa)+"\\").orElse("")+p.getFileName().toString();
  }

  private final String innoEntryDir(final Path dir){
    final String p = winFormat(dir);
    return "Source: \""+p+"\\*\"; DestDir: \"{app}\\"+p+"\"";
  }

  private final String innoEntryFile(final Path baseDir, final Path file){
    final String destination =
      file.getParent().equals(baseDir)
      ? ""
      : winFormat(baseDir.relativize(file.getParent())) + "\\"
    ;
    return "Source: \""+winFormat(baseDir.relativize(file))+"\"; DestDir: \"{app}\""+destination;
  }


}
