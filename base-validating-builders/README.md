# `base` Validating Builders

This module assembles the `ValidatingBuilder`s for the `base` Protobuf definitions.

Since `model-compiler` requires `base`, `base` cannot be assembled by the `model-compiler`
seamlessly.

In order to include the `ValidatingBuilder`s into the `base` artifact, the builders are assembled 
separately in this module and put under the `base-validating-builders/builders` directory.

When assembling the JAR archive, `base` includes the classes under this path.

## Assembling Artifacts

In order to assemble the final artifact for `base`, execute the `:base:build` task. It triggers 
the build of `base-validating-builders` project via the Java process API, which then triggers 
the JAR archive rebuild the same way.

## References

See [`base-validating-builders/build.gradle`](./build.gradle) for the builders assembly.

See [`base/build.gradle`](../base/build.gradle) for the builders usage.
