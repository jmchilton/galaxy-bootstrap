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

  /**
   * Tests deafult BootStrapper is the latest stable.
   * @throws InterruptedException
   * @throws IOException
   */
  @Test
  public void testSetup() throws InterruptedException, IOException {
    final BootStrapper bootStrapper = new BootStrapper();
    
    bootStrapper.setupGalaxy();
    
    // test to make sure we have checked out the latest revision of Galaxy
    String expectedLatestRevision = getTipMercurialRevisionHash(bootStrapper.getPath());
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    assert expectedLatestRevision != null;
    assert expectedLatestRevision.equalsIgnoreCase(actualRevision);
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Tests setup of Galaxy for the latest stable.
   * @throws IOException 
   * @throws InterruptedException 
   */
  @Test
  public void testLatestStable() throws InterruptedException, IOException {
    final BootStrapper bootStrapper = new BootStrapper(
      DownloadProperties.forLatestStable());

    testSetupGalaxyFor(bootStrapper);
    
    // test to make sure we have checked out the latest revision of Galaxy
    String expectedLatestRevision = getTipMercurialRevisionHash(bootStrapper.getPath());
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    assert expectedLatestRevision != null;
    assert expectedLatestRevision.equalsIgnoreCase(actualRevision);
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Tests to make sure downloading Galaxy at a specific revision works
   * @throws IOException 
   * @throws InterruptedException 
   */
  @Test
  public void testSpecificRevision() throws InterruptedException, IOException {
    // Galaxy stable release for 2013.11.04 at https://bitbucket.org/galaxy/galaxy-dist
    final String expectedRevision = "5e605ed6069fe4c5ca9875e95e91b2713499e8ca";
    final BootStrapper bootStrapper = new BootStrapper(
      DownloadProperties.forStableAtRevision(expectedRevision));
    
    testSetupGalaxyFor(bootStrapper);
    
    String actualRevision = getCurrentMercurialRevisionHash(bootStrapper.getPath());
    
    assert expectedRevision.equalsIgnoreCase(actualRevision);
    
    bootStrapper.deleteGalaxyRoot();
  }
  
  /**
   * Tests Galaxy for a specific setup.
   * @param bootStrapper  The BootStrapper used for setting up Galaxy.
   * @throws InterruptedException
   * @throws IOException
   */
  private void testSetupGalaxyFor(BootStrapper bootStrapper) throws InterruptedException, IOException {
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
