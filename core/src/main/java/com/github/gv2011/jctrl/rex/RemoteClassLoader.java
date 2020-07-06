package com.github.gv2011.jctrl.rex;

import static com.github.gv2011.util.ex.Exceptions.notYetImplemented;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

final class RemoteClassLoader extends ClassLoader{

  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    return findClass(null, name);
  }

  @Override
  protected Class<?> findClass(final String moduleName, final String name) {
    // TODO Auto-generated method stub
    return notYetImplemented();
  }

  @Override
  protected URL findResource(final String moduleName, final String name){
    // TODO Auto-generated method stub
    return notYetImplemented();
  }

  @Override
  protected URL findResource(final String name) {
    return findResource(null, name);
  }

  @Override
  protected Enumeration<URL> findResources(final String name) throws IOException {
    // TODO Auto-generated method stub
    return notYetImplemented();
  }



}
