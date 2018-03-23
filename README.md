# base

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/80cf6232764843ef878500e05355d0b4)](https://www.codacy.com/app/SpineEventEngine/base?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=SpineEventEngine/base&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg)](https://codecov.io/gh/SpineEventEngine/base)
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

This repository contains the code of foundation of the Spine Event Engine framework which includes:

* The framework [base](base) data types.
* [Utilities for testing](testlib).
* Gradle plug-ins:
  * [Spine Model Compiler](tools/gradle-plugins/model-compiler) — 
    transforms a domain model defined in proto files into Java code.  
  * [Google Cloud Storage Gradle plugin](tools/gradle-plugins/gcs-plugin) — 
    allows to store build log in a Google Cloud Storage bucket.
  * [Spine Javadoc](tools/gradle-plugins/spine-javadoc) — 
    processes Javadocs of generated files.
  * [Code Style Checker](tools/gradle-plugins/spine-codestyle-checker) — utility plugin for 
    verifying Java code conventions.
  * [Reflections Plugin](tools/gradle-plugins/reflections-plugin) — 
    Gradle port of Maven Reflections plugin required for Reflections framework.
* [Protoc plugin](tools/protoc-plugin) —
    a plug-in for Google Protobuf compiler for generating custom code for framework-specific message
    types.
    
These components are used by [core-java](https://github.com/SpineEventEngine/core-java) and are not
supposed to be used directly by the end user project.


