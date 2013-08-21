package com.github.jmchilton.galaxybootstrap;

import com.google.common.hash.Hashing;
import java.io.File;

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
  
  public GalaxyDaemon run(final GalaxyProperties galaxyProperties) {
    return run(galaxyProperties, null);
  }
  
  public GalaxyDaemon run(final GalaxyProperties galaxyProperties,
                          final GalaxyData galaxyData) {
    galaxyProperties.configureGalaxy(getRoot());
    executeGalaxyScript("python scripts/fetch_eggs.py");
    executeGalaxyScript("sh create_db.sh > /dev/null");
    if(galaxyData != null) {
      galaxyData.writeSeedScript(new File(getRoot(), "seed.py"));
      executeGalaxyScript("python seed.py");
    }
    final Process process = IoUtils.execute("sh", new File(getPath(), "run.sh").getAbsolutePath(), "--daemon");
    return new GalaxyDaemon(galaxyProperties, getRoot());
  }

  public static class GalaxyDaemon {
    private final GalaxyProperties galaxyProperties;
    private final File galaxyRoot;
    
    GalaxyDaemon(final GalaxyProperties galaxyProperties,
                 final File galaxyRoot) {
      this.galaxyProperties = galaxyProperties;
      this.galaxyRoot = galaxyRoot;
    }
    
    public void stop() {
      final Process process = IoUtils.execute("sh", new File(galaxyRoot, "run.sh").getAbsolutePath(), "--stop-daemon");
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
        IoUtils.executeAndWait("hg", "clone", repositoryUrl, cache.getAbsolutePath());
      }
      IoUtils.executeAndWait("hg", "-R", cache.getAbsolutePath(), "pull", "-u");
      repositoryTarget = cache.getAbsolutePath();
    }
    IoUtils.executeAndWait("hg", "clone", repositoryTarget, path);
  }
  
  private void executeGalaxyScript(final String scriptName) {
    final String bashScript = String.format("cd %s; %s", getPath(), scriptName);
    IoUtils.executeAndWait("bash", "-c", bashScript);
  }
  
}
