package com.github.gv2011.jctrl.installerbuilder;

import java.nio.file.Path;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.util.icol.ISortedSet;

public class ModulePathBuilder{

  private final M2t m2t;

  public ModulePathBuilder(final M2t m2t) {
    this.m2t = m2t;
  }

  public static void main(final String[] args){
    try(M2t m2t = M2tFactory.INSTANCE.get().create()){
      final ModulePathBuilder mpb = new ModulePathBuilder(m2t);
      mpb.getAllJars(new InstallerBuilderMarker().artifactRef()).forEach(System.out::println);
    }
  }

  public ISortedSet<Path> getAllJars(final ArtifactRef artifactRef){
    return  m2t.getClasspath(artifactRef, Scope.RUNTIME);
  }

}
