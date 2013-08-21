package com.github.jmchilton.galaxybootstrap;

import java.io.File;
import java.io.IOException;

/** 
 * Defines basic properties used for obtaining Galaxy instance.
 *  
 */
public class DownloadProperties {
  final String repositoryUrl;
  final File location;
  private static final String DEFAULT_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
  boolean cache = true;

  public DownloadProperties(final String repositoryUrl, final File location_) {
    this.repositoryUrl = repositoryUrl;
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

}
