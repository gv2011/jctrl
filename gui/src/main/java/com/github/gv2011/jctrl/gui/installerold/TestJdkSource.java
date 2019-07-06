package com.github.gv2011.jctrl.gui.installerold;

import java.net.URI;
import java.nio.file.Paths;

import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.icol.Opt;

public final class TestJdkSource implements JdkSource{

  @Override
  public URI url() {
    return Paths.get("C:\\Dateien\\Software\\java-12-openjdk-12.0.1.12-1.windows.ojdkbuild.x86_64.zip").toUri();
  }

  @Override
  public Long size() {
    return 254763745L;
  }

  @Override
  public Hash256 hash() {
    return ByteUtils.parseHash("6534807820bd1ed0617ad8ac3d33b30c290f36835ae0f562cc10f3d526f51580");
  }

  @Override
  public Opt<String> rootFolderName() {
    return Opt.of("java-12-openjdk-12.0.1.12-1.windows.ojdkbuild.x86_64/");
  }

}
