# Spine Testlib

This module provides utilities for testing in Spine SDK subprojects. 
These utilities may also be handy for the users of the Spine SDK.

Spine Testlib relies on the following libraries:
  * [Google Protobuf](https://github.com/protocolbuffers/protobuf)
  * [Guava Testlib](https://github.com/google/guava/tree/master/guava-testlib)
  * [JUnit 5](https://junit.org/junit5/)
  * [Google Truth](https://github.com/google/truth) and its Java 8 and Protobuf extensions.

Dependencies on these libraries are exposed using the API scope to simplify dependency
management in user projects. Please see [build.gradle.kts] for details.

## Gradle dependency
To use Spine Testlib in your Gradle project:

```kotlin
dependencies {
    testImplementation("io.spine.tools:spine-testlib:${version}")
}
```
