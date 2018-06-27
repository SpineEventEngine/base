# `base` Validating Builders

This module assembles the `ValidatingBuilder`s for the `base` Protobuf definitions.

Since `model-compiler` requires `base`, `base` cannot be assembled by the `model-compiler`
seamlessly.

In order to include the `ValidatingBuilder`s into the `base` artifact, the builders are assembled 
separately in this module and put under the `base-validating-builders/builders` directory.

When assembling the JAR archive, `base` includes the classes under this path.

## Assembling Artifacts

In order to assemble the final artifact for `base`, execute the following commands:

In the root project:
```bash
./gradlew clean build publishToMavenLocal
```
In `base-validating-builders`:
```bash
./gradlew clean build
```
Again in the root project:
```bash
./gradlew :base:cleanJar :base:jar
```

## References

See [`base-validating-builders/build.gradle`](./build.gradle) for the builders assembly.

See [`base/build.gradle`](../base/build.gradle) for the builders usage.
