package com.github.gv2011.jctrl.installerbuilder;

import static com.github.gv2011.util.icol.ICollections.*;

import java.nio.file.Path;
import java.util.Comparator;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.m2t.Version;
import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.XStream;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.tstr.TypedString;

public class ModulePathBuilder implements AutoCloseableNt{

  private final M2t m2t;

  public ModulePathBuilder(final M2t m2t) {
    this.m2t = m2t;
  }

  public static void main(final String[] args){
    try(ModulePathBuilder mpb = new ModulePathBuilder(M2tFactory.INSTANCE.get().create())){
      mpb.getAllJars(TypedString.create(Version.class, "0.0.1-SNAPSHOT")).forEach(System.out::println);
    }
  }

  public IList<Path> getAllJars(final Version version){
    final M2t m2t = M2tFactory.INSTANCE.get().create();
    final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId   ).to("com.github.gv2011.jctrl")
      .setTStr(ArtifactRef::artifactId).to("jctrl-simple")
      .set(ArtifactRef::version       ).to(version)
      .build()
    ;
    final IList<Path> classpath = XStream.of(m2t.resolve(artifact))
      .concat(m2t.getDependenciesFiles(artifact, Scope.RUNTIME).stream())
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
