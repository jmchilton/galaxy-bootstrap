package com.github.jmchilton.galaxybootstrap;

import com.google.common.hash.Hashing;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BootStrapper {
  private final DownloadProperties downloadProperties;
  private final String galaxyLogDirName = "bootstrap-log";

  public BootStrapper() {
    this(new DownloadProperties());
  }

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
  
  /**
   * Defines a directory to place all log files for Galaxy bootstrap.
   * @return  A directory to place all log files for Galaxy bootstrap.
   */
  private File getBootstrapLogDir() {
    return new File(downloadProperties.location.getPath(), galaxyLogDirName);
  }
  
  /**
   * Constructs a path of a file under the bootstrap log directory.
   * @param bootstrapLogDir  The File defining the log directory.
   * @param logFileName  The name of the log file.
   * @return  The full path to the log file.
   */
  private String buildLogPath(File bootstrapLogDir, String logFileName) {
    return new File(bootstrapLogDir, logFileName).getAbsolutePath();
  }
  
  public GalaxyDaemon run(final GalaxyProperties galaxyProperties,
                          final GalaxyData galaxyData) {
    File bootstrapLogDir = getBootstrapLogDir();
    if (!bootstrapLogDir.exists()) {
      if (!bootstrapLogDir.mkdir()) {
        throw new RuntimeException("Could not make log directory " + bootstrapLogDir);
      }
    }
    
    galaxyProperties.configureGalaxy(getRoot());
    executeGalaxyScript("python scripts/fetch_eggs.py 1> "
      + buildLogPath(bootstrapLogDir,"fetch_eggs.log") + " 2>&1");
    
    executeGalaxyScript("sh create_db.sh 1> " 
      + buildLogPath(bootstrapLogDir,"create_db.log") + " 2>&1");
    
    if(galaxyData != null) {
      galaxyData.writeSeedScript(new File(getRoot(), "seed.py"));
      executeGalaxyScript("python seed.py 1> " 
        + buildLogPath(bootstrapLogDir,"seed.log") + " 2>&1");
    }
    final Process process = IoUtils.execute("sh", new File(getPath(), "run.sh").getAbsolutePath(), "--daemon");
    return new GalaxyDaemon(galaxyProperties, getRoot(), this);
  }
  
  public void deleteGalaxyRoot() {
    IoUtils.executeAndWait("/bin/rm", "-rf", getPath());
  }

  public static class GalaxyDaemon {
    private final GalaxyProperties galaxyProperties;
    private final File galaxyRoot;
    private final BootStrapper bootStrapper;
    
    GalaxyDaemon(final GalaxyProperties galaxyProperties,
                 final File galaxyRoot,
                 final BootStrapper bootStrapper) {
      this.galaxyProperties = galaxyProperties;
      this.galaxyRoot = galaxyRoot;
      this.bootStrapper = bootStrapper;
    }
    
    public BootStrapper getBootStrapper() {
      return bootStrapper;
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
    downloadProperties.download();
  }
  
  private void executeGalaxyScript(final String scriptName) {
    final String bashScript = String.format("cd %s; %s", getPath(), scriptName);
    IoUtils.executeAndWait("bash", "-c", bashScript);
  }
  
}
