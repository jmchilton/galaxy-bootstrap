package com.github.jmchilton.galaxybootstrap;

import com.github.jmchilton.galaxybootstrap.BootStrapper.GalaxyDaemon;
import com.github.jmchilton.galaxybootstrap.GalaxyData.User;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.testng.annotations.Test;

public class BootStrapperTest {

  @Test
  public void testSetup() throws InterruptedException, IOException {
    final BootStrapper bootStrapper = new BootStrapper();
    bootStrapper.setupGalaxy();
    
    // test to make sure we have checked out the latest revision of Galaxy
    String expectedLatestRevision = getTipMercurialRevisionHash(bootStrapper.getPath());
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    assert expectedLatestRevision != null;
    assert expectedLatestRevision.equalsIgnoreCase(actualRevision);
    
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
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Tests to make sure DownloadProperties.forLatestStable() gets correct revision.
   */
  @Test
  public void testLatestStable() {
    final BootStrapper bootStrapper = new BootStrapper(
      DownloadProperties.forLatestStable());
    bootStrapper.setupGalaxy();
    
    // test to make sure we have checked out the latest revision of Galaxy
    String expectedLatestRevision = getTipMercurialRevisionHash(bootStrapper.getPath());
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    assert expectedLatestRevision != null;
    assert expectedLatestRevision.equalsIgnoreCase(actualRevision);
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Tests to make sure downloading Galaxy at a specific revision works
   */
  @Test
  public void testSpecificRevision() {
    // arbitrary revision from stable branch at https://bitbucket.org/galaxy/galaxy-central
    final String expectedRevision = "6c5913a4b701813e823638125fff8bf9fda7354b";
    final BootStrapper bootStrapper = new BootStrapper(
      DownloadProperties.forStableAtRevision(null, expectedRevision));
    bootStrapper.setupGalaxy();
    
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    
    assert expectedRevision.equalsIgnoreCase(actualRevision);
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Given the mercurial root directory gets the current revision hash code checked out.
   * @param mercurialDir  The root mercurial directory.
   * @return  The current revision hash checked out.
   */
  private String getCurrentMercurialRevisionHash(String mercurialDir) {
    String hash = null;
    final String bashScript 
      = "cd " + mercurialDir + "; hg parent --template '{node}'";
    Process p = IoUtils.execute("bash", "-c", bashScript);
    
    hash = convertStreamToString(p.getInputStream());
    
    return hash;
  }
  
  /**
   * Given the mercurial root directory gets the tip revision hash code.
   * @param mercurialDir  The root mercurial directory.
   * @return  The tip revision hash.
   */
  private String getTipMercurialRevisionHash(String mercurialDir) {
    String hash = null;
    final String bashScript 
      = "cd " + mercurialDir + "; hg tip --template '{node}'";
    Process p = IoUtils.execute("bash", "-c", bashScript);
    
    hash = convertStreamToString(p.getInputStream());
    
    return hash;
  }
  
  /**
   * Given an input stream, converts to a String containing all data.
   * @param is  The input stream to read from.
   * @return  A String containing all data from the input stream.
   */
  private String convertStreamToString(InputStream is) {
    Scanner s = new Scanner(is);
    Scanner d = s.useDelimiter("\\A");
    String string = d.hasNext() ? d.next() : "";
    s.close();
    
    return string;
  }
}
