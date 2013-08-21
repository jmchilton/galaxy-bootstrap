
package com.github.jmchilton.galaxybootstrap;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
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
    final CharSource script = Resources.asCharSource(getClass().getResource("seedScript"), Charsets.UTF_8);
    final CharSink charSink = com.google.common.io.Files.asCharSink(scriptPath, Charsets.UTF_8);
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
      }
      charSink.write(scriptBuilder);
    } catch(final IOException ioException) {
      throw new RuntimeException(ioException);
    }
  }
  
  
}
