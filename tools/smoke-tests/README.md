# Smoke tests

Smoke tests module contains the integration tests for the Spine tools.

This module is treated as a separate Gradle project and depends on `main` via Gradle
[composite build](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions).

Tests in this module are run in the separate Gradle process and currently cannot be included in the 
repository code coverage.

They also should be run separately from the `main` project build like this:

```bash
  ./gradlew build check --stacktrace
  
  cd ./tools/smoke-tests
  
  ./gradlew check --stacktrace
```

To configure Intellij Idea for recognizing `smoke-tests` module as the actual source code, see 
[this page](https://blog.jetbrains.com/idea/2016/10/intellij-idea-2016-3-eap-gradle-composite-builds-and-android-studio-2-2/).

### Gradle build scripts

Some Gradle build files in this project rely on `modelCompiler` heavily. We cannot get a type-safe
accessor for the extension. In those build files we use Groovy instead of Kotlin. At alternative
would be using Kotlin and `withGroovyBuilder(..)`.
