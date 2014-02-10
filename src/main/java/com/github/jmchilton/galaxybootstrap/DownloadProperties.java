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
 *  
 */
public class DownloadProperties {
  public static final String GITHUB_MASTER_URL = "https://codeload.github.com/jmchilton/galaxy-central/zip/master";
  public static final String GALAXY_DIST_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
  public static final String GALAXY_CENTRAL_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-central";
  public static final String BRANCH_STABLE = "stable";
  public static final String BRANCH_DEFAULT = "default";
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
  
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final File location) {
    this(new HgDownloader(repositoryUrl, BRANCH_STABLE, HgDownloader.CURRENT_REVISION), location);
  }
  
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final File location) {
    this(new HgDownloader(repositoryUrl, branch, HgDownloader.CURRENT_REVISION), location);
  }
  
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final String revision,
    final File location) {
	  
    this(new HgDownloader(repositoryUrl, branch, revision), location);
  }

  @Deprecated
  public DownloadProperties(final String repositoryUrl) {
    this(new HgDownloader(repositoryUrl, BRANCH_STABLE, HgDownloader.CURRENT_REVISION), null);
  }

  @Deprecated
  public DownloadProperties() {
    this(DEFAULT_REPOSITORY_URL);
  }

  public void setUseCache(final boolean cache) {
    this.cache = cache;
  }

  public static DownloadProperties wgetGithubCentral() {
    return wgetGithubCentral(null);
  }
  
  public static DownloadProperties forGalaxyDist() {
    return forGalaxyDist(null);
  }
  
  public static DownloadProperties forGalaxyCentral() {
    return forGalaxyCentral(null);
  }
  
  public static DownloadProperties forLatestStable() {
    return forLatestStable(null);   
  }

  public static DownloadProperties wgetGithubCentral(final File destination) {
    return new DownloadProperties(new GithubDownloader(), destination);
  }
  
  public static DownloadProperties forGalaxyDist(final File destination) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, destination);
  }
  
  public static DownloadProperties forGalaxyDist(final File destination, String revision) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }
  
  public static DownloadProperties forGalaxyCentral(final File destination) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, destination);
  }
  
  public static DownloadProperties forGalaxyCentral(final File destination, String revision) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }
  
  public static DownloadProperties forLatestStable(final File destination) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, destination);
  }
  
  public static DownloadProperties forStableAtRevision(final File destination, String revision) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }
  
  void download() {
    final String path = location.getAbsolutePath();
    this.downloader.downlaodTo(location, cache);
  }
  

  private interface Downloader {

    void downlaodTo(File path, boolean useCache);

  }

  private static class HgDownloader implements Downloader {
    private static final String CURRENT_REVISION = "";	  

    private final String branch;
    private final String repositoryUrl;
    
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
      }
    
    @Override
    public void downlaodTo(File path, boolean useCache) {
      String repositoryTarget = repositoryUrl;
      if(useCache) {
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
      
      if (!CURRENT_REVISION.equals(revision)) {
        cloneCommand.add("-r");
        cloneCommand.add(revision);
      }
      
      cloneCommand.add(repositoryTarget);
      cloneCommand.add(path.getAbsolutePath());
      IoUtils.executeAndWait(cloneCommand.toArray(new String[0]));
    }

  }
  
  private static class GithubDownloader implements Downloader {

    public void downlaodTo(File path, boolean useCache) {
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
    
  }

  private static class JavaGithubDownloader implements Downloader {
    // WAY TO SLOW.
    @Override
    public void downlaodTo(final File path, final boolean useCache) {

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

  }  
}
