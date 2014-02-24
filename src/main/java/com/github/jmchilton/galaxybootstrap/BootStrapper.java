package com.github.jmchilton.galaxybootstrap;

import java.io.File;

public class BootStrapper {
  private final DownloadProperties downloadProperties;
  private final String galaxyLogDirName = "bootstrap-log";

  /**
   * Builds a bootstrapper object with the default settings.
   */
  public BootStrapper() {
    this(new DownloadProperties());
  }

  /**
   * Builds a BootStrapper object with the given DownloadProperties for downloading of Galaxy.
   * @param downloadProperties  The DownloadProperties used to download Galaxy.
   */
  public BootStrapper(final DownloadProperties downloadProperties) {
    this.downloadProperties = downloadProperties;
  }

  /**
   * Gets the current path of the Galaxy root directory.
   * @return  The path where Galaxy will be installed.
   */
  public String getPath() {
    return downloadProperties.location.getAbsolutePath();
  }
  
  /**
   * Gets a File of the current root Galaxy directory.
   * @return The root directory of Galaxy.
   */
  public File getRoot() {
    return new File(getPath());
  }
  
  /**
   * Runs the current setup of Galaxy with the given properties.
   * @param galaxyProperties  The properties used to run Galaxy.
   * @return  A GalaxyDaemon object used for controlling the Galaxy process.
   */
  public GalaxyDaemon run(final GalaxyProperties galaxyProperties) {
    return run(galaxyProperties, null);
  }
  
  /**
   * Defines a directory to place all log files for Galaxy bootstrap.
   * @return  A directory to place all log files for Galaxy bootstrap.
   */
  public File getBootstrapLogDir() {
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
  
  /**
   * Deletes the Galaxy root directory.
   */
  public void deleteGalaxyRoot() {
    IoUtils.executeAndWait("/bin/rm", "-rf", getPath());
  }

  public static class GalaxyDaemon {
    private final GalaxyProperties galaxyProperties;
    private final File galaxyRoot;
    private final BootStrapper bootStrapper;
    
    /**
     * Builds a new GalaxyDaemon object for controlling the Galaxy process.
     * @param galaxyProperties  An objectin containing properties for Galaxy.
     * @param galaxyRoot  The root directory of Galaxy.
     * @param bootStrapper  An object used for downloading a clean version of Galaxy.
     */
    GalaxyDaemon(final GalaxyProperties galaxyProperties,
                 final File galaxyRoot,
                 final BootStrapper bootStrapper) {
      this.galaxyProperties = galaxyProperties;
      this.galaxyRoot = galaxyRoot;
      this.bootStrapper = bootStrapper;
    }
    
    /**
     * Gets the BootStrapper object.
     * @return  The BootStrapper object.
     */
    public BootStrapper getBootStrapper() {
      return bootStrapper;
    }
    
    /**
     * Stops the currently running Galaxy instance.
     */
    public void stop() {
      final Process process = IoUtils.execute("sh", new File(galaxyRoot, "run.sh").getAbsolutePath(), "--stop-daemon");
      try {
        process.waitFor();
      } catch(InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    /**
     * Checks if Galaxy is currently running.
     * @return  True if Galaxy is running, false otherwise.
     */
    public boolean up() {
      return !IoUtils.available(galaxyProperties.getPort());
    }
    
    /**
     * Waits for Galaxy to start running.
     * @return True if Galaxy was successfully started, false if a timeout occured.
     */
    public boolean waitForUp() {
      return wait(true);
    }
    
    /**
     * Checks if Galaxy has stopped running.
     * @return  True if Galaxy has stopped running, false if a timeout occured.
     */
    public boolean waitForDown() {
      return wait(false);
    }
    
    /**
     * Waits for the given state of the Galaxy process.
     * @param up  The state of the Galaxy process, true if
     *  we should wait for Galaxy to start running, false if
     *  we should wait for Galaxy to stop running.
     * @return  True if the state has been reached, false if a timeout issued occured.
     */
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
  
  
  /**
   * Setup the definied instance of Galaxy.
   */
  public void setupGalaxy() {
    downloadProperties.download();
  }
  
  /**
   * Executes a script within the Galaxy root directory.
   * @param scriptName  The Galaxy script to run.
   */
  private void executeGalaxyScript(final String scriptName) {
    final String bashScript = String.format("cd %s; %s", getPath(), scriptName);
    IoUtils.executeAndWait("bash", "-c", bashScript);
  }
  
}
