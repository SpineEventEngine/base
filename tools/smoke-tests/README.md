# Smoke tests

Smoke tests module contains integration tests for the Spine tools.

This module is treated as a separate Gradle project and depends on `main` via 
[composite build](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions).

Tests of this module are run in the separate Gradle process and currently are not included in the 
repository code coverage.

They also should be run separately from the `main` project build like this:

```bash
  ./gradlew build check --stacktrace
  cd ./tools/smoke-tests
  ../../gradlew check --stacktrace
```

To configure Intellij Idea for recognizing smoke tests as actual source code, see 
[this page](https://blog.jetbrains.com/idea/2016/10/intellij-idea-2016-3-eap-gradle-composite-builds-and-android-studio-2-2/).