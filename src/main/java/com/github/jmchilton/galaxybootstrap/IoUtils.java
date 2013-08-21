package com.github.jmchilton.galaxybootstrap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class IoUtils {
  
  
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
    throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
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
}
