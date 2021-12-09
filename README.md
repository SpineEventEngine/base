[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]

# Spine Event Engine Base

This repository contains the foundation of the Spine framework which includes the following modules:

* **[`base`](base)** — the framework base data types and utilities for working with them.
* **[`testlib`](testlib)** - utilities for testing.

These components are used by [core-java](https://github.com/SpineEventEngine/core-java) and other
Spine libraries. 

They are not supposed to be used directly by the end user project.

## Java Support

Starting version `2.0.0-SNAPSHOT.78`, the modules in this repository are built with Java 11.

Prior versions, including all `1.x` versions were assembled with Java 8.


[gh-actions]: https://github.com/SpineEventEngine/base/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/base/actions/workflows/build-on-ubuntu.yml/badge.svg
[codecov]: https://codecov.io/gh/SpineEventEngine/base
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
