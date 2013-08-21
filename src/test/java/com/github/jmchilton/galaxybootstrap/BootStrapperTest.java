package com.github.jmchilton.galaxybootstrap;

import com.github.jmchilton.galaxybootstrap.DownloadProperties;
import com.github.jmchilton.galaxybootstrap.BootStrapper.GalaxyDaemon;
import com.github.jmchilton.galaxybootstrap.GalaxyData.User;
import org.testng.annotations.Test;

public class BootStrapperTest {

  @Test
  public void testSetup() throws InterruptedException {
    final BootStrapper bootStrapper = new BootStrapper(new DownloadProperties());
    bootStrapper.setupGalaxy();
    final GalaxyProperties galaxyProperties = new GalaxyProperties().assignFreePort();
    final GalaxyData galaxyData = new GalaxyData();
    final User adminUser = new User("admin@localhost");
    final User normalUser = new User("user@localhost");
    galaxyData.getUsers().add(adminUser);
    galaxyData.getUsers().add(normalUser);
    galaxyProperties.setAdminUser("admin@localhost");
    final int port = galaxyProperties.getPort();
    assert IoUtils.available(port);
    final GalaxyDaemon daemon = bootStrapper.run(galaxyProperties, galaxyData);
    assert daemon.waitForUp();
    daemon.stop();
    assert daemon.waitForDown();
  }
  
}
