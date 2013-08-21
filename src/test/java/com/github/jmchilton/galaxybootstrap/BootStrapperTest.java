package com.github.jmchilton.galaxybootstrap;

import com.github.jmchilton.galaxybootstrap.BootStrapper.DownloadProperties;
import com.github.jmchilton.galaxybootstrap.BootStrapper.GalaxyDaemon;
import org.testng.annotations.Test;

public class BootStrapperTest {

  @Test
  public void testSetup() throws InterruptedException {
    final BootStrapper bootStrapper = new BootStrapper(new DownloadProperties());
    bootStrapper.setupGalaxy();
    final GalaxyProperties galaxyProperties = new GalaxyProperties().assignFreePort();
    final int port = galaxyProperties.getPort();
    assert IoUtils.available(port);
    final GalaxyDaemon daemon = bootStrapper.runWithProperties(galaxyProperties);
    assert daemon.waitForUp();
    daemon.stop();
    assert daemon.waitForDown();
  }
  
}
