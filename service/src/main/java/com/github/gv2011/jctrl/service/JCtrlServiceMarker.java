package com.github.gv2011.jctrl.service;

import java.net.URL;

import com.github.gv2011.m2t.AbstractArtifactMarker;

public final class JCtrlServiceMarker extends AbstractArtifactMarker{

  @Override
  protected URL getPomProperties() {
    return getClass().getResource(POM_PROPERTIES);
  }

}
