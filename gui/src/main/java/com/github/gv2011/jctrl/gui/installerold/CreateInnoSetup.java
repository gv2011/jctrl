package com.github.gv2011.jctrl.gui.installerold;

import static com.github.gv2011.util.CollectionUtils.recursiveStream;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.gv2011.util.XStream;
import com.github.gv2011.util.icol.Opt;

public class CreateInnoSetup {

  public static void main(final String[] args) {
    final Path dir = Paths.get("target/jlink");
    call(()->Files.list(dir))
      .flatMap(f->{
        return (XStream<Path>)(
          Files.isDirectory(f)
          ? recursiveStream(f, c->call(()->Files.list(c)).filter(c2->Files.isDirectory(c2)))
          : XStream.of(f)
        );
      })
      .filter(CreateInnoSetup::isFileOrHasFileChildren)
      .map(f->
        Files.isDirectory(f)
        ? innoEntryDir(dir.relativize(f))
        : innoEntryFile(dir, f)
      )
      .forEach(System.out::println)
    ;
  }

  private static final boolean isFileOrHasFileChildren(final Path p){
    return Files.isDirectory(p) ? call(()->Files.list(p)).anyMatch(c->!Files.isDirectory(c)) : true;
  }


  private static final String winFormat(final Path p){
    return Opt.ofNullable(p.getParent()).map(pa->winFormat(pa)+"\\").orElse("")+p.getFileName().toString();
  }

  private static final String innoEntryDir(final Path dir){
    final String p = winFormat(dir);
    return "Source: \""+p+"\\*\"; DestDir: \"{app}\\"+p+"\"";
  }

  private static final String innoEntryFile(final Path baseDir, final Path file){
    final String destination =
      file.getParent().equals(baseDir)
      ? ""
      : winFormat(baseDir.relativize(file.getParent())) + "\\"
    ;
    return "Source: \""+winFormat(baseDir.relativize(file))+"\"; DestDir: \"{app}\\"+destination;
  }


}
