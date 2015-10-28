# binkley's BLOG

[binkley's BLOG](https://binkley.blogspot.com)

This software is in the Public Domain.  Please see [LICENSE.md](LICENSE.md).

Current released version is 6.  [View javadoc](https://binkley.github.io/binkley/).

[![License](https://img.shields.io/badge/license-PD-blue.svg)](http://unlicense.org) [![Build Status](https://img.shields.io/travis/binkley/binkley.svg)](https://travis-ci.org/binkley/binkley) [![Issues](https://img.shields.io/github/issues/binkley/binkley.svg)](https://github.com/binkley/binkley/issues) [![maven-central](https://img.shields.io/maven-central/v/hm.binkley/binkley-blog.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22hm.binkley%22)

## Modules

* [Annotation](annotation/) - Help for Java annotation processors
* [Concurrent](concurrent/) - Completable executor services
* [Convert](convert/) - Inverse of `toString()`
* [CORBA](corba/) - Helpers for CORBA
* [Dynafig](dynafig/README.me) - Dynamic configuation
* [Guice](guice/) - Sample Guice modules and helper code
* [Logging](logging/) - Small logging improvements and OSI logback configuration
* [Lombok](lombok/) - Lombok annotations
* [Magic bus](magic-bus/) - Intraprocess message bus
* [Mixin](mixin/) - Mixins for Java via JDK proxies and method handles
* [Spring](spring/) - Examples with Spring
* [Testing](testing/) - Small testing improvements
* [Utility](util/README.md) - `Bug`, `CheckedStream` and friends
* [Value type](value-type/) - Java annotation and processor for value types
* [Xio](xio/) - Pulling out interfaces from JDK I/O
* [XML](xml/) - Experiments in XML
* [XProperties](xprops/) - Extended Java properties
* [YAML compile](yaml-compile/) - Java code generation from YAML
* [YAML runtime](yaml-runtime/) - Java code generation from YAML

## Changes

### 7

* Add magic bus
* Add dynafig
* Lombok module fixed at lombok 1.14.x.  Internals changed in lombok, this
  code has not caught up
* Guice module fixed at guice 3.0.  Guice API has changed, this code has not
  caught up
* Struggle with Travis CI
* Add Reactor IO based magic bus
* Add Property and friends to util, an alternative to exposing beans

### 6

* Simpler versioning.
* Added `TypesafeHeterogenousMap`.
* Added `StringX` for additional formatting.
* Added annotation module.
* Added yaml module.
* Completable executors can unwrap interrupts.
* Added `Matching` for DSL akin to lesser pattern matching (no implicit destructuring)
* Added `SQLTransactionRule` for running tests in a SQL transaction and rolling back after
* Added `SpringSQLTransactionRule` for Spring-JDBC help with `SQLTransactionRule`

### 0.5

* Added corba module.
* Added `ProvidePort` junit rule.
* Added several Hamcrest matchers (in support of other code).
* Added `StackTraceFocuser` to simplify traces.
* Added `LinkedIterable`.
* Added `ParameterizedHelper` for junit.
* More Java 8-isms.
* Taught `Mixin` to handle interface default methods.
* Added lombok module.
* Added xml module.
* Added `Notices` for Martin Fowler's post.

### 0.4

* Dropped finance module: use JSR 354.
* Fixed issues with support loggers.  OSI logging is no longer beta.
* Default OSI logging level is INFO, not WARN.
* Added support for ANSI codes in logging via OSI and other improvements.
* OSI logging requires a minimum of Java 7 or higher.
* Various converters reorganized.
