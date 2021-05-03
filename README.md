[![Build Status][travis-badge]][travis] &nbsp; 
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]

# Spine Event Engine Base

This repository contains the foundation of the Spine framework which includes the following modules:

* **[`base`](base)** — the framework base data types and utilities for working with them.
* **[`testlib`](testlib)** - utilities for testing.

* **[`tools/javadoc-style`](tools/javadoc-style)** — a Gradle plugin which processes Javadocs of
  generated Java files to make them look less ugly.
* **[`tools/javadoc-filter`](tools/javadoc-filter)** — excludes elements annotated with
  `io.spine.Internal` from the generated doc.
  
  
* **Model Compiler for Java** which consists of the following sub-modules:
  * **[`tools/mc-java`](tools/mc-java)** — a Gradle plugin responsible for enabling Java
    code generation.
  * **[`tools/mc-java-protoc`](tools/mc-java-protoc)** — plugs-into Google Protobuf compiler for
    generating framework-specific Java code.
  *  **[`tools/mc-java-validation`](tools/mc-java-validation)** — `protoc` plugins for generating
    code for validating attributes of a data model.
  * **[`tools/mc-java-checks`](tools/mc-java-checks)** — static code analyzers for Spine-based
  Java projects, implemented as custom Error Prone checks.

    
* **[`tools/mc-js`](tools/mc-js)** — **Model Compiler for JS**, a Gradle plug-in that assists Protobuf
  JS compiler in JavaScript code generation.


* **[`tools/mc-dart`](tools/mc-dart)** — **Model Compiler for Dart**, a Gradle plugin for generating
  Dart code. 

These components are used by [core-java](https://github.com/SpineEventEngine/core-java) and are not
supposed to be used directly by the end user project.

The repository also contains:

* [`base-validating-builders`](base-validating-builders) — a helper module which generates
  validating builders for the proto types declared in the module `base`. This module avoids circular
  dependency because Model Compiler for Java also depends on `base`. 
  See the [module README.md](base-validating-builders/README.md) for more details.
  

* A [common base](tools/plugin-base) for Spine Gradle plugins.
  

* [Test utilities](tools/plugin-testlib) for Spine plugins.
  

* [Integration tests](tests) for all Spine tools.

### Notes on Coverage

The coverage stats (as shown on the Codecov badge above) reflect the hits gathered from
unit tests. However, Gradle plugins — a significant part of this repository — are covered with
integration tests. During each of those, a standalone Gradle process is launched.

The limitations of `jacoco` task API do not allow to include 
the coverage of such tests into the repository coverage report easily.
Therefore, the coverage percentage shown is significantly lower than a real one.
So, don't be afraid, this code is tested as the rest of the framework.

### `pull` scripts

In most Spine repositories, we update the `config` submodule by running `./config/pull` (or its
Batch equivalent). However, in `base` we also need to copy Gradle `buildSrc` directory into included
builds: `tests` and `base-validating-builders`. Thus, here we have `./pull` and `.\pull.bat`
scripts which do whatever their `config` counterparts do and also copy `buildSrc` into the two
included build directories.

It is always recommended running `./pull` instead of `./config/pull`.


[travis]: https://travis-ci.com/SpineEventEngine/base
[travis-badge]: https://travis-ci.com/SpineEventEngine/base.svg?branch=master
[codecov]: https://codecov.io/gh/SpineEventEngine/base
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
