package com.github.gv2011.jctrl.installerbuilder;

import java.net.URL;

import com.github.gv2011.m2t.AbstractArtifactMarker;

public final class InstallerBuilderMarker extends AbstractArtifactMarker{

  @Override
  protected URL getPomProperties() {
    return getClass().getResource(POM_PROPERTIES);
  }

}
