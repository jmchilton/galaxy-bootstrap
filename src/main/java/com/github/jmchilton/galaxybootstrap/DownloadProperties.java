package com.github.jmchilton.galaxybootstrap;

import java.io.File;
import java.io.IOException;

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

}
