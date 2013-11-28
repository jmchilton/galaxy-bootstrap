package com.github.jmchilton.galaxybootstrap;

import com.github.jmchilton.galaxybootstrap.BootStrapper.GalaxyDaemon;
import com.github.jmchilton.galaxybootstrap.GalaxyData.User;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.testng.annotations.Test;

public class BootStrapperTest {

  @Test
  public void testSetup() throws InterruptedException, IOException {
    final BootStrapper bootStrapper = new BootStrapper();
    bootStrapper.setupGalaxy();
    final GalaxyProperties galaxyProperties = 
      new GalaxyProperties()
            .assignFreePort()
            .configureNestedShedTools();
    final GalaxyData galaxyData = new GalaxyData();
    final User adminUser = new User("admin@localhost");
    final User normalUser = new User("user@localhost");
    galaxyData.getUsers().add(adminUser);
    galaxyData.getUsers().add(normalUser);
    galaxyProperties.setAdminUser("admin@localhost");
    galaxyProperties.setAppProperty("allow_library_path_paste", "true");
    galaxyProperties.prepopulateSqliteDatabase();
    final int port = galaxyProperties.getPort();
    assert IoUtils.available(port);
    final GalaxyDaemon daemon = bootStrapper.run(galaxyProperties, galaxyData);
    final File shedToolsFile = new File(bootStrapper.getRoot(), "shed_tool_conf.xml");    
    final String shedToolsContents = Files.toString(shedToolsFile, Charsets.UTF_8);
    final URL shedToolConfResource = getClass().getResource("shed_tool_conf.xml");
    final String expectedShedToolsContents = Resources.toString(shedToolConfResource, Charsets.UTF_8);
    assert shedToolsContents.equals(expectedShedToolsContents);
    assert new File(bootStrapper.getRoot(), "shed_tools").isDirectory();
    assert daemon.waitForUp();
    daemon.stop();
    assert daemon.waitForDown();
    
  }
  
}
