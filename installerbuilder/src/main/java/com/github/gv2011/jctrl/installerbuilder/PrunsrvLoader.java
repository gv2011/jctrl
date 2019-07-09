package com.github.gv2011.jctrl.installerbuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.m2t.Type;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.Zipper;
import com.github.gv2011.util.icol.Opt;

public class PrunsrvLoader{

  public static void main(final String[] args) {
    try(M2t m2t = M2tFactory.INSTANCE.get().create()){
      new PrunsrvLoader(m2t).loadPrunsrv(Paths.get("target", "image", "bin"));
    }
  }

  private final M2t m2t;


  PrunsrvLoader(final M2t m2t) {
    this.m2t = m2t;
  }


  public Path loadPrunsrv(final Path directory) {
    final ArtifactRef daemonWin = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId   ).to("commons-daemon")
      .setTStr(ArtifactRef::artifactId).to("commons-daemon")
      .setTStr(ArtifactRef::version   ).to("1.2.0")
      .setTStr(ArtifactRef::classifier).to("bin-windows")
      .set    (ArtifactRef::type      ).to(Type.ZIP)
      .build()
    ;
    final Path file = m2t.resolve(daemonWin);
    final Path expected = directory.resolve("amd64/prunsrv.exe");
    Zipper.newZipper().unZipWithFilter(
      ()->Files.newInputStream(file),
      directory,
      p->p.equals(expected) ? Opt.of(directory.resolve("prunsrv.exe")) : Opt.empty()
    );
    return file;
  }

}
