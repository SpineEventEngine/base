[![Build Status](https://travis-ci.com/SpineEventEngine/base.svg?branch=master)](https://travis-ci.com/SpineEventEngine/base) &nbsp;
[![codecov](https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg)](https://codecov.io/gh/SpineEventEngine/base) &nbsp;
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

# This is 2.x `master` branch!

This branch contains the changes which aren't meant to be merged to `master` 
at least until the Spine 2.0.0 version release.

The code in this branch is **not production-ready**. Please use the released versions of 
Spine 1.x instead. The API modifications are significant. Some features aren't backward-compatible
with the 1.x functionality.

The codebase built on top of the `base` modules in this branch is designed for **JDK 8 runtime**.

This branch must be treated as `master` for 2.x features, so the changes to it must go
through the PR review process.
 
## Versioning in 2.x branch

While preparing the release of the version 2.0.0 of the framework, the libraries are versioned 
as follows:

* `2.0.0-jdk8.SNAPSHOT.N` — a version of the libraires in-development; `N` is a positive integer 
which is incremented upon each changeset/PR;
* `2.0.0-jdk8.rc1`, `2.0.0-jdk8.rc2` etc  — the versions of the preliminary releases; 
each of these may be followed up by more `SNAPSHOT`s;
* `2.0.0-jdk8` — the first release of the Spine v2 for JDK 8 runtime.    

# Spine Event Engine Base

This repository contains the code of foundation of the Spine Event Engine framework which includes:

* The framework [base](base) data types and utilities.
* [Utilities for testing](testlib).
* Gradle plug-ins:
  * [Spine Model Compiler](tools/model-compiler) — transforms a domain model defined in proto files 
    into the Java code.
  * [Spine Javadoc Prettifier](tools/javadoc-prettifier) — processes Javadocs of generated files.
* [Protoc plugin](tools/protoc-plugin) — a plug-in for Google Protobuf compiler for generating 
  custom code for framework-specific message types.
* [Proto JS plugin](tools/proto-js-plugin) — a plug-in that assists Protobuf JS compiler in 
  JavaScript code generation.
* [Spine Javadoc Filter](tools/javadoc-filter) — excludes elements annotated with 
  `io.spine.Internal` from the generated doc.
* [Spine Error Prone Checks](tools/errorprone-checks) — the custom Error Prone checks for the
  Spine projects.

These components are used by [core-java](https://github.com/SpineEventEngine/core-java) and are not
supposed to be used directly by the end user project.

The repository also contains:

* Validating Builders [assembler](base-validating-builders) for `base`.
* A [common base](tools/plugin-base) for Spine Gradle plugins.
* [Test utilities](tools/plugin-testlib) for Spine plugins.
* [Integration tests](tools/smoke-tests) for all Spine tools.

### Notes on Coverage

Currently, the coverage stats reflect the hits gathered from unit tests. However, Gradle plugins - 
a significant part of this repository - are covered with integration tests. During each of those, a 
standalone Gradle process is launched. The limitations of `jacoco` task API do not allow to include 
the coverage of such tests into the repository coverage report easily. Therefore, current coverage 
percentage shown is significantly lower than a real one.

### `pull` scripts

In most Spine repositories, we update the `config` submodule by running `./config/pull` (or its
Batch equivalent). However, in `base` we also need to copy Gradle `buildSrc` directory into included
builds: `smoke-tests` and `base-validating-builders`. Thus, here we have `./pull` and `.\pull.bat`
scripts which do whatever their `config` counterparts do and also copy `buildSrc` into the two
included build directories.

It is always recommended to run `./pull` instead of `./config/pull`.
