galaxy-bootstrap
================

[![Build Status](https://travis-ci.org/jmchilton/galaxy-bootstrap.png?branch=master)](https://travis-ci.org/jmchilton/galaxy-bootstrap)

galaxy-boostrap is a small Java library allowing for programmatic
download and configuration of the [Galaxy
framework](http://galaxyproject.org). The initial goal of this project
is to be used for the automated testing of
[blend4j](http://github.com/jmchilton/blend4j), but it may have some
interesting applications (when paired with blend4j) in terms of using
Galaxy as computational platform within the context of a JVM
application.

### Requirements

- Java 6
- Maven 
- Mercurial

### Logging

Logging is provided by [log4j](http://logging.apache.org/log4j/).  Additional information can be displayed by setting the level to DEBUG.  For example:

```bash
mvn test -Dlog4j.logger.com.github.jmchilton.galaxybootstrap=DEBUG
```
