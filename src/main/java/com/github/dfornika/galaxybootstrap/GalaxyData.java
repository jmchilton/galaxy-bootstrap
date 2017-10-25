
package com.github.dfornika.galaxybootstrap;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Chilton
 */
public class GalaxyData {
  
  private static final Logger logger = LoggerFactory
      .getLogger(GalaxyData.class);
  
  public static class User {
    private String username;
    private String password = "123456";
    private String apiKey = UUID.randomUUID().toString();

    public User(String username) {
      this.username = username;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }
    
    
  }
  
  private final Set<User> users = Sets.newHashSet();
  
  public Set<User> getUsers() {
    return users;
  }
  
  public void writeSeedScript(final File scriptPath) {
    final CharSource script = Resources.asCharSource(getClass().getResource("seedScript"), Charsets.UTF_8);
    final CharSink charSink = Files.asCharSink(scriptPath, Charsets.UTF_8);
    try {
      final StringBuilder scriptBuilder = new StringBuilder();
      scriptBuilder.append(script.read());
      for(final User user : users) {
        final StringBuilder line = new StringBuilder();
        line.append("add_user('");
        line.append(user.username);
        line.append("', '");
        line.append(user.password);
        line.append("', '");
        line.append(user.apiKey);
        line.append("')\n");
        scriptBuilder.append(line);
        
        logger.debug("Adding user: " + line);
      }
      charSink.write(scriptBuilder);
    } catch(final IOException ioException) {
      throw new RuntimeException(ioException);
    }
  }
  
  
}
