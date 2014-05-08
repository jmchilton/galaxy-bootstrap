package com.github.jmchilton.galaxybootstrap;

import com.google.common.hash.Hashing;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** 
 * Defines basic properties used for obtaining Galaxy instance.
 */
public class DownloadProperties {
	
  public static final String GITHUB_MASTER_URL = "https://codeload.github.com/jmchilton/galaxy-central/zip/master";
  public static final String GALAXY_DIST_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
  public static final String GALAXY_CENTRAL_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-central";
  public static final String BRANCH_STABLE = "stable";
  public static final String BRANCH_DEFAULT = "default";
  
  /**
   * Defines a constant for specifying that Galaxy should be downloaded with the latest revision.
   */
  public static final String LATEST_REVISION = "";

  private static final String DEFAULT_REPOSITORY_URL = GALAXY_DIST_REPOSITORY_URL;
  private final Downloader downloader;
  final File location;
  boolean cache = true;

  /**
   * Builds a new DownloadProperties object defining how to download Galaxy.
   * @param downloader The method to use for downloading Galaxy.
   * @param location_  The location of the filesystem to store and setup Galaxy.
   */
  DownloadProperties(final Downloader downloader, final File location_) {
    this.downloader = downloader;
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
  
  /**
   * Builds a new DownloadProperties on the stable branch with the given URL and location.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   * @param location  The location to store Galaxy, null if a directory should be chosen automatically.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final File location) {
    this(new HgDownloader(repositoryUrl, BRANCH_STABLE, LATEST_REVISION), location);
  }
  
  /**
   * Builds a new DownloadProperties on the given branch with the given URL and location.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   * @param branch  The branch of the repository to use.
   * @param location  The location to store Galaxy, null if a directory should be chosen automatically.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final File location) {
    this(new HgDownloader(repositoryUrl, branch, LATEST_REVISION), location);
  }
  
  /**
   * Builds a new DownloadProperties on the given branch with the given URL and location.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   * @param branch  The branch of the repository to use.
   * @param revision  The revision of Galaxy to use.
   * @param location  The location to store Galaxy, null if a directory should be chosen automatically.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final String revision,
    final File location) {
	  
    this(new HgDownloader(repositoryUrl, branch, revision), location);
  }

  /**
   * Builds a new DownloadProperties on the stable branch with the given URL with an
   *  automatically chosen local directory to store Galaxy.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl) {
    this(new HgDownloader(repositoryUrl, BRANCH_STABLE, LATEST_REVISION), null);
  }

  /**
   * Builds a new DownloadProperties with the default settings for downloading Galaxy.
   */
  @Deprecated
  public DownloadProperties() {
    this(DEFAULT_REPOSITORY_URL);
  }

  /**
   * Whether or not a local cache of downloaded Galaxies should be created. 
   * @param cache  True if a cache should be used, false otherwise.
   */
  public void setUseCache(final boolean cache) {
    this.cache = cache;
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubCentral() {
    return wgetGithubCentral(null);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-dist.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-dist.
   */
  public static DownloadProperties forGalaxyDist() {
    return forGalaxyDist(null, LATEST_REVISION);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-dist with a specific revision.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-dist.
   */
  public static DownloadProperties forGalaxyDistAtRevision(String revision) {
    return forGalaxyDist(null, revision);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  public static DownloadProperties forGalaxyCentral() {
    return forGalaxyCentral(null, LATEST_REVISION);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central with a specific revision.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  public static DownloadProperties forGalaxyCentralAtRevision(String revision) {
    return forGalaxyCentral(null, revision);
  }
  
  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forLatestStable() {
    return forLatestStable(null);
  }
  
  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy with a specific revision.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forStableAtRevision(String revision) {
    return forStableAtRevision(null, revision);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubCentral(final File destination) {
    return new DownloadProperties(new GithubDownloader(), destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-dist.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-dist.
   */
  public static DownloadProperties forGalaxyDist(final File destination) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-dist.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-dist.
   */
  public static DownloadProperties forGalaxyDist(final File destination, String revision) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  public static DownloadProperties forGalaxyCentral(final File destination) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_DEFAULT, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  public static DownloadProperties forGalaxyCentral(final File destination, String revision) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_DEFAULT, revision, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forLatestStable(final File destination) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy at the given revision.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy at the given revision.
   */
  public static DownloadProperties forStableAtRevision(final File destination, String revision) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }
  
  /**
   * Performs the download of Galaxy.
   */
  void download() {
    final String path = location.getAbsolutePath();
    this.downloader.downloadTo(location, cache);
  }

  @Override
  public String toString() {
    return "Galaxy Download: " + downloader + ", location=" + location + ", use cache=" + cache;
  }

  /**
   * Defines an interface for implementations of classes to download Galaxy.
   */
  private interface Downloader {

    void downloadTo(File path, boolean useCache);

  }

  /**
   *  Defines a downloader to download Galaxy from Mercurial. 
   */
  private static class HgDownloader implements Downloader {
    private final String branch;
    private final String repositoryUrl;
    private final File cacheDir;
    
    /**
     * Revision to checkout, if null assumes we are interested in most recent. 
     */
    private final String revision;
    
    public HgDownloader(final String repositoryUrl, final String branch, final String revision) {
      if (revision == null) {
        throw new IllegalArgumentException("revision is null");
      }
    	
      this.branch = branch;
      this.repositoryUrl = repositoryUrl;
      this.revision = revision;
      
      this.cacheDir = getCacheDir(repositoryUrl);
    }
    
    /**
     * Gets the directory of the Galaxy repository cache.
     * @param repositoryUrl  The url used to constructe the cache directory.
     * @return  The Galaxy repository cache.
     */
    private File getCacheDir(String repositoryUrl) {
      final String repoHash = Hashing.md5().hashString(repositoryUrl).toString();
      final File cache = new File(Config.home(), repoHash);
      
      return cache;
    }

	  @Override
    public void downloadTo(File path, boolean useCache) {
      String repositoryTarget = repositoryUrl;
      if(useCache) {
        if(!cacheDir.exists()) {
          cacheDir.getParentFile().mkdirs();
          IoUtils.executeAndWait("hg", "clone", repositoryUrl, cacheDir.getAbsolutePath());
        }
        IoUtils.executeAndWait("hg", "-R", cacheDir.getAbsolutePath(), "pull", "-u");
        repositoryTarget = cacheDir.getAbsolutePath();
      }
      final List<String> cloneCommand = new ArrayList<String>();
      cloneCommand.add("hg");
      cloneCommand.add("clone");
      if(branch != null) {
        cloneCommand.add("-b");
        cloneCommand.add(branch);
      }
      
      if (!LATEST_REVISION.equals(revision)) {
        cloneCommand.add("-r");
        cloneCommand.add(revision);
      }
      
      cloneCommand.add(repositoryTarget);
      cloneCommand.add(path.getAbsolutePath());
      IoUtils.executeAndWait(cloneCommand.toArray(new String[0]));
    }
	
    @Override
    public String toString() {
      String revision = (LATEST_REVISION.equals(this.revision) ? "latest" : this.revision);
      return "Mercurial [repositoryUrl=" + repositoryUrl + ", branch=" 
          + branch + ", revision=" + revision + ", cacheDir=" + cacheDir.getAbsolutePath() +"]";
    }
  }
  
  /**
   * Defines a downloader to download Galaxy from github.
   */
  private static class GithubDownloader implements Downloader {

    public void downloadTo(File path, boolean useCache) {
      try {
        final File downloadDest = File.createTempFile("gxdownload", ".zip");
        final File unzipDest = File.createTempFile("gxdownload", "dir");
        final String unzippedDirectory = String.format("%s/galaxy-central-master", unzipDest.getAbsolutePath());
        IoUtils.executeAndWait("wget", GITHUB_MASTER_URL, "-O", downloadDest.getAbsolutePath());
        unzipDest.delete();
        IoUtils.executeAndWait("unzip", "-o", "-qq", downloadDest.getAbsolutePath(), "-d", unzipDest.getAbsolutePath());
        path.delete();
        IoUtils.executeAndWait("mv", unzippedDirectory, path.getAbsolutePath());
        IoUtils.executeAndWait("rm", "-rf", unzipDest.getAbsolutePath(), downloadDest.getAbsolutePath());
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    @Override
    public String toString() {
      return "GithubDownloader [url=" + GITHUB_MASTER_URL + ", branch=master]";
    }
  }

  /**
   * Defines a downloader to download Galaxy from github using Java.
   */
  private static class JavaGithubDownloader implements Downloader {
    // WAY TO SLOW.
    @Override
    public void downloadTo(final File path, final boolean useCache) {

      try {
        final URL download = new URL(GITHUB_MASTER_URL);
        ZipInputStream zis = new ZipInputStream(download.openStream());
        ZipEntry ze = zis.getNextEntry();
        byte[] buffer = new byte[1024];
        while(ze!=null){
 
    	   String fileName = ze.getName().substring("galaxy-central-master/".length());
           if(fileName.equals("")) {
             continue;
           }
           File newFile = new File(path + File.separator + fileName);
 
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();
 
            FileOutputStream fos = new FileOutputStream(newFile);             
 
            int len;
            while ((len = zis.read(buffer)) > 0) {
       		fos.write(buffer, 0, len);
            }
 
            fos.close();   
            ze = zis.getNextEntry();
    	}
 
        zis.closeEntry();
    	zis.close();
 
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    @Override
    public String toString() {
      return "JavaGithubDownloader [url=" + GITHUB_MASTER_URL + ", branch=master]";
    }
  }  
}
