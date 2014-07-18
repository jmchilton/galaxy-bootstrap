package com.github.jmchilton.galaxybootstrap;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IoUtils {
  
  private static final Logger logger = LoggerFactory.getLogger(IoUtils.class); 
  
  /**
   * Returns a free port number on localhost.
   *
   * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
   * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
   *
   * https://gist.github.com/vorburger/3429822
   * 
   * @return a free port number on localhost
   * @throws IllegalStateException if unable to find a free port
   */
  public static int findFreePort() {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      socket.setReuseAddress(true);
      int port = socket.getLocalPort();
      try {
        socket.close();
      } catch(IOException e) {
        // Ignore IOException on close()
      }
      return port;
    } catch(IOException e) {
    } finally {
      if(socket != null) {
        try {
          socket.close();
        } catch(IOException e) {
        }
      }
    }
    throw new IllegalStateException("Could not find a free TCP/IP port to start Galaxy on");
  }
  
  static boolean available(int port) {
    Socket s = null;
    try {
      s = new Socket("localhost", port);

      // If the code makes it this far without an exception it means
      // something is using the port and has responded.
      return false;
    } catch(IOException e) {
      return true;
    } finally {
      if(s != null) {
        try {
          s.close();
        } catch(IOException e) {
          throw new RuntimeException("You should handle this error.", e);
        }
      }
    }
  }

  static void executeAndWait(final String[] commands, final Map<String, String> properties) {
    try {
      final ProcessBuilder builder = new ProcessBuilder(commands);
      String commandString = Joiner.on(" ").join(commands);
      
      if(properties != null) {
        builder.environment().putAll(properties);
      }
      logger.debug("Executing command: \"" + commandString + "\"");
      final Process p = builder.start();
      final int returnCode = p.waitFor();
      if(returnCode != 0) {
        final String message = "Execution of command [%s] failed.";
        throw new RuntimeException(String.format(message, commandString));
      }
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    } catch(InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  static void executeAndWait(final String... commands) {
    executeAndWait(commands, null);
  }
  
  static Process execute(final String... commands) {
    final ProcessBuilder builder = new ProcessBuilder(commands);
    final Process process;
    String commandString = Joiner.on(" ").join(commands);
    
    try {
      logger.debug("Executing command: \"" + commandString + "\"");
      process = builder.start();
    } catch(IOException ex) { 
      throw new RuntimeException(ex);
    }
    return process;
  }
  
}
