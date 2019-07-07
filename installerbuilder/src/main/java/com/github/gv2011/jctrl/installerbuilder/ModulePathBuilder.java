package com.github.gv2011.jctrl.installerbuilder;

import static com.github.gv2011.util.icol.ICollections.toIList;

import java.nio.file.Path;
import java.util.Comparator;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.XStream;
import com.github.gv2011.util.icol.IList;

public class ModulePathBuilder implements AutoCloseableNt{

  private final M2t m2t;

  public ModulePathBuilder(final M2t m2t) {
    this.m2t = m2t;
  }

  public static void main(final String[] args){
    try(ModulePathBuilder mpb = new ModulePathBuilder(M2tFactory.INSTANCE.get().create())){
      mpb.getAllJars(new InstallerBuilderMarker().artifactRef()).forEach(System.out::println);
    }
  }

  public IList<Path> getAllJars(final ArtifactRef artifactRef){
    final IList<Path> classpath = XStream.of(m2t.resolve(artifactRef))
      .concat(m2t.getDependenciesFiles(artifactRef, Scope.RUNTIME).stream())
      .distinct()
      .sorted(Comparator.comparing(Path::toString))
      .collect(toIList())
    ;
    return classpath;
  }

  @Override
  public void close() {
    m2t.close();
  }
}
