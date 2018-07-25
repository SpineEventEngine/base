# base

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/80cf6232764843ef878500e05355d0b4)](https://www.codacy.com/app/SpineEventEngine/base?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=SpineEventEngine/base&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg)](https://codecov.io/gh/SpineEventEngine/base)
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

This repository contains the code of foundation of the Spine Event Engine framework which includes:

* The framework [base](base) data types. // todo Add readme
* [Validating Builders](base-validating-builders) for `base` Protobuf definitions.
* [Utilities for testing](testlib). // todo Add readme
* Gradle plug-ins:
  * [Spine Model Compiler](tools/model-compiler) — 
    transforms a domain model defined in proto files into the Java code. // todo Add readme
  * [Spine Javadoc Prettifier](tools/javadoc-prettifier) — processes Javadocs of generated files. // todo Add readme
  * [Reflections Plugin](tools/reflections-plugin) — 
    Gradle port of Maven Reflections plugin required for Reflections framework. // todo Add readme
* [Protoc plugin](tools/protoc-plugin) —
    a plug-in for Google Protobuf compiler for generating custom code for framework-specific message
    types.
* [Spine Javadoc Filter](tools/javadoc-filter) — excludes elements annotated with 
  `io.spine.Internal` from the generated doc. // todo Add readme

These components are used by [core-java](https://github.com/SpineEventEngine/core-java) and are not
supposed to be used directly by the end user project.

This repository also contains:

* A [common base](tools/plugin-base) for Spine Gradle plugins. // todo Add readme
* [Test utilities](tools/plugin-testlib) for Spine plugins. // todo Add readme
* [Integration tests](tools/smoke-tests) for all Spine tools. // todo Add readme


**Coverage**

Currently the coverage stats reflect the hits gathered from unit tests. 

However, Gradle plugins, that are a significant part of this repository, are covered with 
integration tests. During each of those a standalone Gradle process is launched. The limitations of 
`jacoco` task API do not allow to include the coverage of such tests into the repository coverage 
report easily. 

Therefore the current coverage percentage shown is significantly lower than a real one.

This issue will be addressed with the new API in Gradle 5.0 once it is released.

