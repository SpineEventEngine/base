[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]

# Spine Base

This repository contains common data types and utilities used by
the Spine SDK subprojects (e.g. [core-java][core-java]).

## Adding to a Gradle project

Spine Base is not supposed to be used directly in an end-user project.
But if you need to, here's how you add it to your Gradle project:

```kotlin
dependencies {
    implementation("io.spine:spine-base:$version")
}
```

## Java Support

Starting version `2.0.0-SNAPSHOT.78`, the modules in this repository are built with Java 11.

Prior versions, including all `1.x` versions were assembled with Java 8.


[gh-actions]: https://github.com/SpineEventEngine/base/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/base/actions/workflows/build-on-ubuntu.yml/badge.svg
[codecov]: https://codecov.io/gh/SpineEventEngine/base
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
[core-java]: https://github.com/SpineEventEngine/core-java 
