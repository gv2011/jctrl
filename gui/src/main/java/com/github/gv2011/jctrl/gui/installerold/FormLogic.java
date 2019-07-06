package com.github.gv2011.jctrl.gui.installerold;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import com.github.gv2011.util.icol.Opt;

abstract class FormLogic {

  static final Function<String, Opt<Path>> defaultFileSystem(){
    return
      p->{
        try {
          return Opt.of(Paths.get(p));
        } catch (final InvalidPathException e) {
          return Opt.empty();
        }
      }
    ;
  }

  final Function<String, Opt<Path>> fileSystem;

  FormLogic(final Function<String, Opt<Path>> fileSystem){
    this.fileSystem = fileSystem;
  }

  final Opt<Path> tryGetDirPath(final String path){
    return
      Opt.of(path)
      .flatMap(fileSystem)
      .flatMap(p->{
        try {return Opt.of(p.toRealPath());}
        catch (final IOException e) {return Opt.empty();}
      })
      .flatMap(p->Files.isDirectory(p) ? Opt.of(p) : Opt.empty())
    ;
  }


}
