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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Defines basic properties used for obtaining Galaxy instance.
 */
public class DownloadProperties {
	
  private static final Logger logger = LoggerFactory
  		.getLogger(DownloadProperties.class);

  public static final String GITHUB_ZIP_URL = "https://codeload.github.com/galaxyproject/galaxy/zip/";
  public static final String GITHUB_ZIP_MASTER_URL = GITHUB_ZIP_URL+ "master";
  public static final String GALAXY_GITHUB_REPOSITORY_URL = "https://github.com/galaxyproject/galaxy.git";
  public static final String GALAXY_DIST_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
  public static final String GALAXY_CENTRAL_REPOSITORY_URL = "https://bitbucket.org/galaxy/galaxy-central";
  public static final String BRANCH_STABLE = "stable";
  public static final String BRANCH_DEFAULT = "default";
  public static final String BRANCH_MASTER= "master";
  public static final String BRANCH_RELEASE_17_01 = "release_17.01";
  public static final String BRANCH_RELEASE_17_05 = "release_17.05";
  public static final String BRANCH_RELEASE_17_09 = "release_17.09";
  public static final String TAG_RELEASE_17_01 = "v17.01";
  public static final String TAG_RELEASE_17_05 = "v17.05";
  public static final String TAG_RELEASE_17_09 = "v17.09";
  public static final String BRANCH_RELEASE_LATEST = BRANCH_RELEASE_17_09;
  public static final String TAG_RELEASE_LATEST = TAG_RELEASE_17_09;

  /**
   * Defines a constant for specifying that Galaxy should be downloaded with the latest commit.
   */
  public static final String LATEST_REVISION = "";
  public static final String LATEST_COMMIT = "";


  private static final String DEFAULT_REPOSITORY_URL = GALAXY_GITHUB_REPOSITORY_URL;

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
    this(new GitGithubDownloader(repositoryUrl, BRANCH_RELEASE_LATEST, LATEST_COMMIT), location);

  }
  
  /**
   * Builds a new DownloadProperties on the given branch with the given URL and location.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   * @param branch  The branch of the repository to use.
   * @param location  The location to store Galaxy, null if a directory should be chosen automatically.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final File location) {
    this(new GitGithubDownloader(repositoryUrl, branch, LATEST_COMMIT), location);
  }
  
  /**
   * Builds a new DownloadProperties on the given branch with the given URL and location.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   * @param branch  The branch of the repository to use.
   * @param commit  The commit of Galaxy to use.
   * @param location  The location to store Galaxy, null if a directory should be chosen automatically.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl, final String branch, final String commit,
    final File location) {
	  
    this(new GitGithubDownloader(repositoryUrl, branch, commit), location);
  }

  /**
   * Builds a new DownloadProperties on the stable branch with the given URL with an
   *  automatically chosen local directory to store Galaxy.
   * @param repositoryUrl  The URL to the Galaxy repository to connect to.
   */
  @Deprecated
  public DownloadProperties(final String repositoryUrl) {
    this(new GitGithubDownloader(repositoryUrl, BRANCH_RELEASE_LATEST, LATEST_COMMIT), null);

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
   * Builds a new DownloadProperties for downloading Galaxy from github master branch (stable) using wget.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubMaster() {
    return wgetGithubMaster(null);
  }
  /**
   * Builds a new DownloadProperties for downloading Galaxy from github master branch (stable) using wget.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties gitGithubMaster() {
    return gitGithubMaster(null);
  }
  /**
   * Builds a new DownloadProperties for downloading Galaxy from github dev branch using wget.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubDev() {
    return wgetGithubDev(null);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  @Deprecated
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
  @Deprecated
  public static DownloadProperties forGalaxyDistAtRevision(String revision) {
    return forGalaxyDist(null, revision);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  @Deprecated
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
  @Deprecated
  public static DownloadProperties forLatestStable() {
    return forLatestStable(null);
  }

  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy with a specific revision.
   * @param revision The revision to use for Galaxy.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  @Deprecated
  public static DownloadProperties forStableAtRevision(String revision) {
    return forStableAtRevision(null, revision);
  }

  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forLatestRelease() {
    return forLatestRelease(null);
  }
  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy with a specific commit.
   * @param commit The commit to use for Galaxy.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forLatestReleaseAtCommit(String commit) {
    return forLatestReleaseAtCommit(null, commit);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  @Deprecated
  public static DownloadProperties wgetGithubCentral(final File destination) {
    return new DownloadProperties(new WgetGithubDownloader(), destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubMaster(final File destination) {
    return DownloadProperties.wgetGithub("master", destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties gitGithubMaster(final File destination) {
    return DownloadProperties.gitGithub("master", destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithubDev(final File destination) {
    return DownloadProperties.wgetGithub("dev", destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param branch The branch to download (e.g. master, dev, release_15.03).
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties wgetGithub(final String branch, final File destination) {
    return new DownloadProperties(new WgetGithubDownloader(branch), destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from github using wget.
   * @param branch The branch to download (e.g. master, dev, release_15.03).
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return  A DownloadProperties for downloading Galaxy from github using wget.
   */
  public static DownloadProperties gitGithub(final String branch, final File destination) {
    return new DownloadProperties(new GitGithubDownloader(GALAXY_GITHUB_REPOSITORY_URL, branch, LATEST_COMMIT), destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-dist.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-dist.
   */
  @Deprecated
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
  @Deprecated
  public static DownloadProperties forGalaxyDist(final File destination, String revision) {
    return new DownloadProperties(GALAXY_DIST_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy from galaxy-central.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy from galaxy-central.
   */
  @Deprecated
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
  @Deprecated
  public static DownloadProperties forGalaxyCentral(final File destination, String revision) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_DEFAULT, revision, destination);
  }

  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  @Deprecated
  public static DownloadProperties forLatestStable(final File destination) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, destination);
  }

  /**
   * Builds a new DownloadProperties for downloading Galaxy at the given revision.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy at the given revision.
   */
  @Deprecated
  public static DownloadProperties forStableAtRevision(final File destination, String revision) {
    return new DownloadProperties(GALAXY_CENTRAL_REPOSITORY_URL, BRANCH_STABLE, revision, destination);
  }

  /**
   * Builds a new DownloadProperties for downloading the latest stable Galaxy.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading the latest stable Galaxy.
   */
  public static DownloadProperties forLatestRelease(final File destination) {
    return new DownloadProperties(GALAXY_GITHUB_REPOSITORY_URL, BRANCH_RELEASE_LATEST, destination);
  }
  
  /**
   * Builds a new DownloadProperties for downloading Galaxy at the given commit.
   * @param destination The destination directory to store Galaxy, null if a directory
   *  should be chosen by default.
   * @return A new DownloadProperties for downloading Galaxy at the given commit.
   */
  public static DownloadProperties forLatestReleaseAtCommit(final File destination, String commit) {
    return new DownloadProperties(GALAXY_GITHUB_REPOSITORY_URL, BRANCH_RELEASE_LATEST, commit, destination);
  }
  
  /**
   * Performs the download of Galaxy.
   */
  void download() {
    final String path = location.getAbsolutePath();
    
    logger.info("About to download Galaxy from " + downloader.toString()
        + " to " + path);
    this.downloader.downloadTo(location, cache);
    logger.info("Finished downloading Galaxy to " + path);
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
  @Deprecated
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
      final String repoHash = Hashing.md5().hashUnencodedChars(repositoryUrl).toString();
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
   * Defines a downloader to download Galaxy from GitHub with wget.
   */
  private static class WgetGithubDownloader implements Downloader {
    private final String branch;
    
    WgetGithubDownloader() {
      this("master");
    }
    
    WgetGithubDownloader(final String branch) {
      this.branch = branch;
    }
    
    public void downloadTo(File path, boolean useCache) {
      try {
        final File downloadDest = File.createTempFile("gxdownload", ".zip");
        final File unzipDest = File.createTempFile("gxdownload", "dir");
        final String unzippedDirectory = String.format("%s/galaxy-%s", unzipDest.getAbsolutePath(), this.branch);
        IoUtils.executeAndWait("wget", GITHUB_ZIP_URL + this.branch, "-O", downloadDest.getAbsolutePath());
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
      return "GithubDownloader [url=" + GITHUB_ZIP_URL + this.branch + ", branch=" + this.branch + "]";
    }
  }

  /**
   *  Defines a downloader to download Galaxy from GitHub with git.
   */
  private static class GitGithubDownloader implements Downloader {
    private final String branch;
    private final String repositoryUrl;
    private final File cacheDir;

    /**
     * Commit to checkout, if null assumes we are interested in most recent.
     */
    private final String commit;

    public GitGithubDownloader(final String repositoryUrl, final String branch, final String commit) {
      if (commit == null) {
        throw new IllegalArgumentException("commit is null");
      }

      this.branch = branch;
      this.repositoryUrl = repositoryUrl;
      this.commit = commit;

      this.cacheDir = getCacheDir(repositoryUrl);
    }

    /**
     * Gets the directory of the Galaxy repository cache.
     * @param repositoryUrl  The url used to construct the cache directory.
     * @return  The Galaxy repository cache.
     */
    private File getCacheDir(String repositoryUrl) {
      final String repoHash = Hashing.md5().hashUnencodedChars(repositoryUrl).toString();
      final File cache = new File(Config.home(), repoHash);

      return cache;
    }

    @Override
    public void downloadTo(File path, boolean useCache) {
      String repositoryTarget = repositoryUrl;
      if(useCache) {
        if(!cacheDir.exists()) {
          cacheDir.getParentFile().mkdirs();
          IoUtils.executeAndWait("git", "clone", repositoryUrl, cacheDir.getAbsolutePath());
        }
        IoUtils.executeAndWait("git", "-C", cacheDir.getAbsolutePath(), "pull", "--all");
        IoUtils.executeAndWait("git", "-C", cacheDir.getAbsolutePath(), "fetch", "--all", "--tags", "--prune");

        IoUtils.executeAndWait("git", "-C", cacheDir.getAbsolutePath(), "checkout", branch);
        repositoryTarget = cacheDir.getAbsolutePath();
      }
      final List<String> cloneCommand = new ArrayList<String>();
      cloneCommand.add("git");
      cloneCommand.add("clone");
      if(branch != null) {
        cloneCommand.add("-b");
        cloneCommand.add(branch);
      }

      cloneCommand.add(repositoryTarget);
      cloneCommand.add(path.getAbsolutePath());
      IoUtils.executeAndWait(cloneCommand.toArray(new String[0]));
      if (!commit.equals("")) {
        IoUtils.executeAndWait("git", "-C", path.getAbsolutePath(), "reset", "--hard", commit);
      }
    }

    @Override
    public String toString() {
      String commit = (LATEST_COMMIT.equals(this.commit) ? "latest" : this.commit);
      return "GitGithubDownloader [repositoryUrl=" + repositoryUrl + ", branch="
              + branch + ", commit=" + commit + ", cacheDir=" + cacheDir.getAbsolutePath() +"]";
    }
  }

  /**
   * Defines a downloader to download Galaxy from github using Java.
   */
  @SuppressWarnings("unused")
  @Deprecated
  private static class JavaGithubDownloader implements Downloader {
    // WAY TO SLOW.
    @Override
    public void downloadTo(final File path, final boolean useCache) {

      try {
        final URL download = new URL(GITHUB_ZIP_MASTER_URL);
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
      return "JavaGithubDownloader [url=" + GITHUB_ZIP_MASTER_URL + ", branch=master]";
    }
  }
}
