package com.github.jmchilton.galaxybootstrap;

import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootStrapper {
  private final DownloadProperties downloadProperties;

  public BootStrapper(final DownloadProperties downloadProperties) {
    this.downloadProperties = downloadProperties;
  }

  public String getPath() {
    return downloadProperties.location.getAbsolutePath();
  }
  
  public File getRoot() {
    return new File(getPath());
  }
  
  public GalaxyDaemon runWithProperties(final GalaxyProperties galaxyProperties) {
    galaxyProperties.configureGalaxy(getRoot());
    executeGalaxyScript("python scripts/fetch_eggs.py");
    executeGalaxyScript("sh create_db.sh > /dev/null");
    final Process process = execute("sh", new File(getPath(), "run.sh").getAbsolutePath(), "--daemon");
    return new GalaxyDaemon(galaxyProperties, getRoot());
  }

  public class GalaxyDaemon {
    private final GalaxyProperties galaxyProperties;
    private final File galaxyRoot;
    
    GalaxyDaemon(final GalaxyProperties galaxyProperties,
                 final File galaxyRoot) {
      this.galaxyProperties = galaxyProperties;
      this.galaxyRoot = galaxyRoot;
    }
    
    public void stop() {
      final Process process = execute("sh", new File(galaxyRoot, "run.sh").getAbsolutePath(), "--stop-daemon");
      try {
        process.waitFor();
      } catch(InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    public boolean up() {
      return !IoUtils.available(galaxyProperties.getPort());
    }
    
    public boolean waitForUp() {
      return wait(true);
    }
    
    public boolean waitForDown() {
      return wait(false);
    }
    
    private boolean wait(final boolean up) {
      boolean correctState = false;
      for(int i = 0; i < 600; i++) {
        correctState = up() == up;
        if(correctState) {
          break;
        }
        try {
          Thread.sleep(1000L);
        } catch(InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      }
      return correctState;
    }

  }
  
  
  public void setupGalaxy() {
    final String repositoryUrl = downloadProperties.repositoryUrl;
    final String path = getPath();
    String repositoryTarget = repositoryUrl;
    if(downloadProperties.cache) {
      final String repoHash = Hashing.md5().hashString(repositoryUrl).toString();
      final File cache = new File(Config.home(), repoHash);
      if(!cache.exists()) {
        cache.getParentFile().mkdirs();
        executeAndWait("hg", "clone", repositoryUrl, cache.getAbsolutePath());
      }
      executeAndWait("hg", "-R", cache.getAbsolutePath(), "pull", "-u");
      repositoryTarget = cache.getAbsolutePath();
    }
    executeAndWait("hg", "clone", repositoryTarget, path);
  }
  
  private void executeAndWait(final String... commands) {
    executeAndWait(commands, null);
  }
  
  private Process execute(final String... commands) {
    final ProcessBuilder builder = new ProcessBuilder(commands);
    final Process process;
    try {
      process = builder.start();
    } catch(IOException ex) { 
      throw new RuntimeException(ex);
    }
    return process;
  }
  
  private void executeGalaxyScript(final String scriptName) {
    final String bashScript = String.format("cd %s; %s", getPath(), scriptName);
    executeAndWait("bash", "-c", bashScript);
  }

  private void executeAndWait(final String[] commands, final Map<String, String> properties) {
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

  static class DownloadProperties {
    private final String repositoryUrl;
    private final File location;
    private static final String DEFAULT_REPOSITORY_URL =
                                "https://bitbucket.org/galaxy/galaxy-dist";
    private boolean cache = true;
    
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
