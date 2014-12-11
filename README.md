binkley's BLOG
==============

<//binkley.blogspot.com>

This software is in the Public Domain.  Please see [LICENSE.md](LICENSE.md).

Current released version is 0.4.  [View javadoc](//binkley.github.io/binkley/).

[![License](//img.shields.io/badge/license-PD-blue.svg?style=flat)](//unlicense.org) [![Build Status](//img.shields.io/travis/binkley/binkley.svg?style=flat)](//travis-ci.org/binkley/binkley)[![issues](//img.shields.io/github/issues/binkley/binkley.svg)](//github.com/binkley/binkley/issues) [![maven-central](//img.shields.io/maven-central/v/hm.binkley/binkley-blog.svg?style=flat)](//search.maven.org/#search%7Cgav%7C1%7Cg%3A%22hm.binkley%22%20AND%20a%3A%22binkley-blog%22)

## Modules

* [Convert](convert/) - Inverse of `toString()`
* [Guice](guice/) - Sample Guice modules and helper code
* [Logging](logging/) - Small logging improvements and OSI logback configuration
* [Mixin](mixin/) - Mixins for Java via JDK proxies and method handles
* [Spring](spring/) - Examples with Spring
* [Testing](testing/) - Small testing improvements
* [Utility](util/) - `Bug`, `CheckedStream` and friends
* [Value type](value-type/) - Java annotation and processor for value types
* [Xio](xio/) - Pulling out interfaces from JDK I/O
* [Xproperties](xprops/) - Extended Java properties

## Changes

### 0.4

* Dropped finance module: use JSR 354.
* Fixed issues with support loggers.  OSI logging is no longer beta.
* Default OSI logging level is INFO, not WARN.
* Added support for ANSI codes in logging via OSI and other improvements.
* OSI logging requires a minimum of Java 7 or higher.
* Various converters reorganized.
