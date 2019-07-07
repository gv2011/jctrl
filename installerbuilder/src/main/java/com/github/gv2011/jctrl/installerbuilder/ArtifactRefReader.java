package com.github.gv2011.jctrl.installerbuilder;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.PropertyUtils;
import com.github.gv2011.util.PropertyUtils.SafeProperties;

public class ArtifactRefReader {

  public final ArtifactRef readArtifactRef(){
    final SafeProperties props =
      PropertyUtils.readProperties(()->ArtifactRefReader.class.getResourceAsStream("pom.properties"))
    ;
    return BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId   ).to(props.getProperty("groupId"))
      .setTStr(ArtifactRef::artifactId).to(props.getProperty("artifactId"))
      .setTStr(ArtifactRef::version   ).to(props.getProperty("version"))
      .build()
    ;
  }

}
