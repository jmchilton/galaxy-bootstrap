package com.github.jmchilton.galaxybootstrap;

import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 
 * Defines basic properties used for obtaining Galaxy instance.
 *  
 */
public class DownloadProperties {
  public static final String GALAXY_DIST_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
  public static final String GALAXY_CENTRAL_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-central";
  public static final String BRANCH_STABLE = "stable";
  public static final String BRANCH_DEFAULT = "default";
  private static final String DEFAULT_REPOSITORY_URL = GALAXY_DIST_REPOSITORY_URL;
  final String branch;
  final String repositoryUrl;
  final File location;
  boolean cache = true;

  public DownloadProperties(final String repositoryUrl, final File location) {
    this(repositoryUrl, BRANCH_STABLE, location);
  }
  
  public DownloadProperties(final String repositoryUrl, final String branch, final File location_) {
    this.repositoryUrl = repositoryUrl;
    this.branch = branch;
    File location = location_;
    if(location == null) {
      try {
        location = File.createTempFile("gxbootstrap", "");
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

  public void setUseCache(final boolean cache) {
    this.cache = cache;
  }
  
  public static DownloadProperties forGalaxyDist() {
    return new DownloadProperties();
  }
  
  public static DownloadProperties forGalaxyCentral() {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_DEFAULT, null);
  }  

  void download() {
    final String path = location.getAbsolutePath();
    String repositoryTarget = repositoryUrl;
    if(cache) {
      final String repoHash = Hashing.md5().hashString(repositoryUrl).toString();
      final File cache = new File(Config.home(), repoHash);
      if(!cache.exists()) {
        cache.getParentFile().mkdirs();
        IoUtils.executeAndWait("hg", "clone", repositoryUrl, cache.getAbsolutePath());
      }
      IoUtils.executeAndWait("hg", "-R", cache.getAbsolutePath(), "pull", "-u");
      repositoryTarget = cache.getAbsolutePath();
    }
    final List<String> cloneCommand = new ArrayList<String>();
    cloneCommand.add("hg");
    cloneCommand.add("clone");
    if(branch != null) {
      cloneCommand.add("-b");
      cloneCommand.add(branch);
    }
    cloneCommand.add(repositoryTarget);
    cloneCommand.add(path);
    IoUtils.executeAndWait(cloneCommand.toArray(new String[0]));
  }
  
}
