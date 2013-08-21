
package com.github.jmchilton.galaxybootstrap;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.FileWriteMode;
import java.io.File;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author John Chilton
 */
public class GalaxyData {
  
  public static class User {
    private String username;
    private String password = "123456";
    private String apiKey = UUID.randomUUID().toString();

    public User(String username) {
      this.username = username;
    }
    
  }
  
  private final Set<User> users = Sets.newHashSet();
  
  public Set<User> getUsers() {
    return users;
  }
  
  public void writeSeedScript(final File scriptPath) {
    //Object resource = getClass().getResource("seedScript");
    //com.google.common.io.Files.asCharSink(scriptPath, Charsets.UTF_8).openStream();
  }
  
  
}
