package com.github.gv2011.jctrl.installer;

import java.net.URI;

import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.icol.Opt;

public interface JdkSource {

  URI url();

  Long size();

  Hash256 hash();

  Opt<String> rootFolderName();

}
