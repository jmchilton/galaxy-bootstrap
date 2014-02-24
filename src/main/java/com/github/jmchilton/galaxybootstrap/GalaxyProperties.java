
package com.github.jmchilton.galaxybootstrap;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 *
 * @author John Chilton
 */
public class GalaxyProperties {
  private final Map<String, String> appProperties = Maps.newHashMap();
  private final Map<String, String> serverProperties = Maps.newHashMap();
  private int port = 8080;  // default
  private boolean configureNestedShedTools = false;
  private Optional<URL> database = Optional.absent();
  
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
    setAppProperty("admin_users", usernamesStr);
  }

  public void configureGalaxy(final File galaxyRoot) {
    try {
      if(configureNestedShedTools) {
        final File shedConf = new File(galaxyRoot, "shed_tool_conf.xml");
        final InputSupplier<InputStream> shedToolConfSupplier =  Resources.newInputStreamSupplier(getClass().getResource("shed_tool_conf.xml"));
        Files.copy(shedToolConfSupplier, shedConf);
        new File(galaxyRoot, "shed_tools").mkdirs();
      }

      final File sampleIni = new File(galaxyRoot, "universe_wsgi.ini.sample");
      final Ini ini = new Ini(new FileReader(sampleIni));
      final Section appSection = ini.get("app:main");
      final boolean toolsConfigured = appProperties.containsKey("tool_config_file");
      if(!toolsConfigured && configureNestedShedTools) {
        appProperties.put("tool_config_file", "tool_conf.xml,shed_tool_conf.xml");
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
      final File configIni = new File(galaxyRoot, "universe_wsgi.ini");
      ini.store(configIni);
      
      final File databaseDirectory = new File(galaxyRoot, "database");
      final File sqliteDatabase = new File(databaseDirectory, "universe.sqlite");
      if(this.database.isPresent()) {
        final URL database = this.database.get();
        Files.copy(Resources.newInputStreamSupplier(database), sqliteDatabase);
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

}
