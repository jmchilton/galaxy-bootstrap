
package com.github.jmchilton.galaxybootstrap;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Chilton
 */
@SuppressWarnings("deprecation")
public class GalaxyProperties {
  public static enum ConfigureVirtualenv {
    YES, NO, AUTO;
  }

  private static final Logger logger = LoggerFactory
      .getLogger(GalaxyProperties.class);
  
  private final Map<String, String> appProperties = Maps.newHashMap();
  private final Map<String, String> serverProperties = Maps.newHashMap();
  private int port = 8080;  // default
  private String galaxyURL = adjustGalaxyURL(port);
  private boolean configureNestedShedTools = false;
  private ConfigureVirtualenv configureVirtualenv = ConfigureVirtualenv.AUTO;
  private Optional<URL> database = Optional.absent();
  
  private static final String CONFIG_DIR_NAME = "config";
  
  private static String adjustGalaxyURL(int port) {
    return "http://localhost:" + port + "/";
  }
  
  public GalaxyProperties setAppProperty(final String name, final String value) {
    appProperties.put(name, value);
    return this;
  }

  public GalaxyProperties setServerProperty(final String name, final String value) {
    serverProperties.put(name, value);
    return this;
  }
  
  public GalaxyProperties prepopulateSqliteDatabase() {
    return prepopulateSqliteDatabase(Resources.getResource(GalaxyProperties.class, "universe.sqlite"));
  }
  
  /**
   * 
   * @return True if it should be inferred that Galaxy is targeting a brand
   *  new database and create_db.sh should be executed.
   */
  public boolean isCreateDatabaseRequired() {
    // Logic in here could be better, database_url may be set and pointing at
    // an existing database - so there should be an option to disable this
    // without specifing a prepopulated sqlite database.
    return !database.isPresent();
  }
  
  public GalaxyProperties prepopulateSqliteDatabase(final URL database) {
    this.database = Optional.of(database);
    // Set database auto migrate to true so database
    // is upgraded from revision in jar if needed.
    setAppProperty("database_auto_migrate", "true");
    return this;
  }
  
  public GalaxyProperties assignFreePort() {
    port = IoUtils.findFreePort();
    serverProperties.put("port", Integer.toString(port));
    galaxyURL = adjustGalaxyURL(port);
    return this;
  }
  
  public GalaxyProperties configureNestedShedTools() {
    this.configureNestedShedTools = true;
    return this;
  }
  
  public void setAdminUser(final String username) {
    setAdminUsers(Lists.newArrayList(username));
  }
  
  public void setAdminUsers(final Iterable<String> usernames) {
    final String usernamesStr = Joiner.on(",").join(usernames);
    logger.debug("Setting admin users: " + usernamesStr);
    setAppProperty("admin_users", usernamesStr);
  }
  
  /**
   * Determines if a virtualenv should be created for Galaxy.
   * @return True iff a virtualenv should be created.
   */
  public boolean shouldConfigureVirtualenv() {
    if(this.configureVirtualenv == ConfigureVirtualenv.NO) {
      return false;
    } else if(this.configureVirtualenv == ConfigureVirtualenv.YES) {
      return true;
    } else {
      final Optional<File> whichVirtualenv = this.which("virtualenv");
      return whichVirtualenv.isPresent();
    }
  }

  /**
   * Determines if this is a pre-2014.10.06 release of Galaxy.
   * @param galaxyRoot  The root directory of Galaxy.
   * @return  True if this is a pre-2014.10.06 release of Galaxy, false otherwise.
   */
  public boolean isPre20141006Release(File galaxyRoot) {
    if (galaxyRoot == null) {
      throw new IllegalArgumentException("galaxyRoot is null");
    } else if (!galaxyRoot.exists()) {
      throw new IllegalArgumentException("galaxyRoot=" + galaxyRoot.getAbsolutePath() + " does not exist");
    }
    
    File configDirectory = new File(galaxyRoot, CONFIG_DIR_NAME);
    return !(new File(configDirectory, "galaxy.ini.sample")).exists();
  }
  
  /**
   * Gets the sample config ini for this Galaxy installation.
   * @param galaxyRoot  The root directory of Galaxy.
   * @return  A File object for the sample config ini for Galaxy.
   */
  private File getConfigSampleIni(File galaxyRoot) {
    if (isPre20141006Release(galaxyRoot)) {
      return new File(galaxyRoot, "universe_wsgi.ini.sample");
    } else {
      File configDirectory = new File(galaxyRoot, CONFIG_DIR_NAME);
      return new File(configDirectory, "galaxy.ini.sample");
    }
  }
  
  /**
   * Gets the config ini for this Galaxy installation.
   * @param galaxyRoot  The root directory of Galaxy.
   * @return  A File object for the config ini for Galaxy.
   */
  private File getConfigIni(File galaxyRoot) {
    if (isPre20141006Release(galaxyRoot)) {
      return new File(galaxyRoot, "universe_wsgi.ini");
    } else {
      File configDirectory = new File(galaxyRoot, CONFIG_DIR_NAME);
      return new File(configDirectory, "galaxy.ini");
    }
  }
  
  /**
   * Gets path to a config file for Galaxy relative to the Galaxy root directory.
   *  This will check for the existence of the file and attempt to copy it from a *.sample file
   *  if it does not exist.
   * @param galaxyRoot  The Galaxy root directory.
   * @param configFileName  The name of the config file to get.
   * @return  The path to the config file relative to the Galaxy root.
   * @throws IOException  If the copy failed.
   */
  private String getConfigPathFromRoot(File galaxyRoot, String configFileName) throws IOException {
    if (isPre20141006Release(galaxyRoot)) {
      return configFileName;
    } else {
      File configDirectory = new File(galaxyRoot, CONFIG_DIR_NAME);
      File toolConf = new File(configDirectory, configFileName);
      
      // if config file does not exist, copy it from the .sample version
      if (!toolConf.exists()) {
        File toolConfSample = new File(configDirectory, configFileName + ".sample");
        Files.copy(toolConfSample, toolConf);
      }
      
      return CONFIG_DIR_NAME + "/" + configFileName;
    }
  }
  
  /**
   * Gets a path for the tool_conf.xml file relative to the Galaxy root.
   * @param galaxyRoot  The Galaxy root directory.
   * @return  The path to the tool_conf.xml file.
   * @throws IOException  If there was an error copying the *.sample file.
   */
  private String getToolConfigPathFromRoot(File galaxyRoot) throws IOException {
    return getConfigPathFromRoot(galaxyRoot, "tool_conf.xml");
  }
  
  /**
   * Gets a path for the shed_tool_conf.xml file relative to the Galaxy root.
   * @param galaxyRoot  The Galaxy root directory.
   * @return  The path to the shed_tool_conf.xml file.
   * @throws IOException  If there was an error copying the *.sample file.
   */
  private String getShedToolConfigPathFromRoot(File galaxyRoot) throws IOException {
    return getConfigPathFromRoot(galaxyRoot, "shed_tool_conf.xml");
  }

  
  public void configureGalaxy(final File galaxyRoot) {
    try {
      if(configureNestedShedTools) {
        final File shedConf = new File(galaxyRoot, "shed_tool_conf.xml");
        final ByteSource shedToolByteSource = Resources.asByteSource(getClass().getResource("shed_tool_conf.xml"));
        shedToolByteSource.copyTo(Files.asByteSink(shedConf));
        new File(galaxyRoot, "shed_tools").mkdirs();
      }

      File sampleIni = getConfigSampleIni(galaxyRoot);
      File configIni = getConfigIni(galaxyRoot);
      final Ini ini = new Ini(new FileReader(sampleIni));
      final Section appSection = ini.get("app:main");
      final boolean toolsConfigured = appProperties.containsKey("tool_config_file");
      if(!toolsConfigured && configureNestedShedTools) {
        String toolConfPath = getToolConfigPathFromRoot(galaxyRoot);
        String shedToolConfPath = getShedToolConfigPathFromRoot(galaxyRoot);
        appProperties.put("tool_config_file", toolConfPath + "," + shedToolConfPath);
      }
      // Hack to work around following bug: https://trello.com/c/nKxmP6Vc
      // Without this, galaxy will not startup because of problems
      // with tool migration framework.
      if(!appProperties.containsKey("running_functional_tests")) {
        appProperties.put("running_functional_tests", "true");
      }              
      dumpMapToSection(appSection, appProperties);
      final Section serverSection = ini.get("server:main");
      dumpMapToSection(serverSection, serverProperties);
      ini.store(configIni);
      
      final File databaseDirectory = new File(galaxyRoot, "database");
      final File sqliteDatabase = new File(databaseDirectory, "universe.sqlite");
      if(this.database.isPresent()) {
        final URL database = this.database.get();
        Resources.asByteSource(database).copyTo(Files.asByteSink(sqliteDatabase));
      }
    } catch(final IOException ioException) {
      throw new RuntimeException(ioException);
    }
  }

  private void dumpMapToSection(final Section section, final Map<String, String> values) {
    section.putAll(values);
  }

  public int getPort() {
    return port;
  }
  
  public String getGalaxyURL() {
    return galaxyURL;
  }

  //http://www.coderanch.com/t/279002/java-io/java/find-executable-path
  private Optional<File> which(final String executableName) {
    String systemPath = System.getenv("PATH");
    String[] pathDirs = systemPath.split(File.pathSeparator);
    
    Optional<File> fullyQualifiedExecutable = Optional.absent();
    for(final String pathDir : pathDirs) {
      File file = new File(pathDir, executableName);
      if (file.isFile()) {
        fullyQualifiedExecutable = Optional.of(file);
        break;
      }
    }
    return fullyQualifiedExecutable;
  }

}
