binkley's BLOG
==============

<http://binkley.blogspot.com>

This software is in the Public Domain.  Please see [LICENSE.md](LICENSE.md).

Current released version is 0.4.  [View javadoc](//binkley.github.io/binkley/).

[![License](http://img.shields.io/badge/license-PD-blue.svg?style=flat)](http://unlicense.org) [![Build Status](http://img.shields.io/travis/binkley/binkley.svg?style=flat)](https://travis-ci.org/binkley/binkley) [![Code Climate](https://img.shields.io/codeclimate/github/binkley/binkley.svg?style=flat)](https://codeclimate.com/github/binkley/binkley) [![Coverage](https://img.shields.io/codeclimate/coverage/github/binkley/binkley.svg?style=flat) [![maven-central](https://img.shields.io/maven-central/v/hm.binkley/binkley-blog.svg?style=flat)

## Modules

* [Convert](convert/) - Inverse of `toString()`
* [CORBA](corba/) - Helpers for CORBA
* [Guice](guice/) - Sample Guice modules and helper code
* [Logging](logging/) - Small logging improvements and OSI logback configuration
* [Lombok](lombok/) - Lombok annotations
* [Mixin](mixin/) - Mixins for Java via JDK proxies and method handles
* [Spring](spring/) - Examples with Spring
* [Testing](testing/) - Small testing improvements
* [Utility](util/) - `Bug`, `CheckedStream` and friends
* [Value type](value-type/) - Java annotation and processor for value types
* [Xio](xio/) - Pulling out interfaces from JDK I/O
* [Xproperties](xprops/) - Extended Java properties

## Changes

### 0.5

* Added corba module.
* Added `ProvidePort` junit rule.
* Added `StackTraceFocuser` to simplify traces.
* Added `LinkedIterable`.
* Added `ParameterizedHelper` for junit.
* More Java 8-isms.
* Taught `Mixin` to handle interface default methods.
* Added lombok module.

### 0.4

* Dropped finance module: use JSR 354.
* Fixed issues with support loggers.  OSI logging is no longer beta.
* Default OSI logging level is INFO, not WARN.
* Added support for ANSI codes in logging via OSI and other improvements.
* OSI logging requires a minimum of Java 7 or higher.
* Various converters reorganized.
