package com.github.jmchilton.galaxybootstrap;

import com.github.jmchilton.galaxybootstrap.BootStrapper.DownloadProperties;
import java.io.File;
import org.testng.annotations.Test;

public class BootStrapperTest {

  @Test
  public void testSetup() {
    final BootStrapper bootStrapper = new BootStrapper(new DownloadProperties());
    bootStrapper.setupGalaxy();
    final File runFile = new File(bootStrapper.getPath(), "run.sh");
    assert runFile.exists();
  }
  
}
