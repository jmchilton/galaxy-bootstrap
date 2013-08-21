
package com.github.jmchilton.galaxybootstrap;

import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
  
  public GalaxyProperties setAppProperty(final String name, final String value) {
    appProperties.put(name, value);
    return this;
  }

  public GalaxyProperties setServerProperty(final String name, final String value) {
    serverProperties.put(name, value);
    return this;
  }
  
  public GalaxyProperties assignFreePort() {
    port = IoUtils.findFreePort();
    serverProperties.put("port", Integer.toString(port));
    return this;
  }

  public void configureGalaxy(final File galaxyRoot) {
    try {
      final File sampleIni = new File(galaxyRoot, "universe_wsgi.ini.sample");
      final Ini ini = new Ini(new FileReader(sampleIni));
      final Section appSection = ini.get("app:main");
      dumpMapToSection(appSection, appProperties);
      final Section serverSection = ini.get("server:main");
      dumpMapToSection(serverSection, serverProperties);
      final File configIni = new File(galaxyRoot, "universe_wsgi.ini");
      ini.store(configIni);
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
