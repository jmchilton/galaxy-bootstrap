package com.github.jmchilton.galaxybootstrap;

import java.io.File;


class Config {

  static File home() {
    return new File(System.getProperty("user.home"), ".galaxy-bootstrap");
  }
  
}
