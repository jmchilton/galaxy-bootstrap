package com.github.jmchilton.galaxybootstrap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BootStrapper {
  private final DownloadProperties downloadProperties;

  public BootStrapper(final DownloadProperties downloadProperties) {
    this.downloadProperties = downloadProperties;
  }

  public String getPath() {
    return downloadProperties.location.getAbsolutePath();
  }

  public void setupGalaxy() {
    final String repositoryUrl = downloadProperties.repositoryUrl;
    final String path = getPath();
    execute("hg", "clone", repositoryUrl, path);
    final File confDirectory = new File(path, "conf.d");
    confDirectory.mkdir();
    final File defaults = new File(path, "universe_wsgi.ini.sample");
    
  }
  
  public void execute(final String... commands) {
    execute(commands, null);
  }

  public void execute(final String[] commands, final Map<String, String> properties) {
    try {
      final ProcessBuilder builder = new ProcessBuilder(commands);
      if(properties != null) {
        builder.environment().putAll(properties);
      }
      final Process p = builder.start();
      final int returnCode = p.waitFor();
      if(returnCode != 0) {
        final String message = "Execution of command [%s] failed.";
        throw new RuntimeException(String.format(message, commands[0]));
      }
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    } catch(InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static class DownloadProperties {
    private final String repositoryUrl;
    private final File location;
    private static final String DEFAULT_REPOSITORY_URL =
                                "https://bitbucket.org/galaxy/galaxy-dist";

    public DownloadProperties(final String repositoryUrl,
                              final File location_) {
      this.repositoryUrl = repositoryUrl;
      File location = location_;
      if(location == null) {
        try {
          location = File.createTempFile("blendtest", "");
          location.delete();
          location.mkdirs();
        } catch(IOException ex) {
          throw new RuntimeException(ex);
        }
      }
      this.location = location;
    }

    public DownloadProperties(final String repositoryUrl) {
      this(repositoryUrl, null);
    }

    public DownloadProperties() {
      this(DEFAULT_REPOSITORY_URL);
    }
  }
}
