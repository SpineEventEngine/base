## Spine Java Checks
This module contains custom [Error Prone](https://github.com/google/error-prone) checks used in 
Spine. All the checks are automatically included in the `JavaCompile` process and produce the 
corresponding warnings and errors.

## Usage
To use the custom Error Prone Checks, add the module dependency to the `annotationProcessor` 
configuration of the project:

```groovy
dependencies {
    annotationProcessor "io.spine.tools:spine-mc-java-checks:$spineBaseVersion"
}
```

Note that the checks, in order to work, require the 
[Error Prone plugin](https://plugins.gradle.org/plugin/net.ltgt.errorprone) to be applied to the 
project.

## Spine Error Prone Checks Plugin
Currently, the Spine-custom Error Prone checks are applied automatically to any project that uses 
[Spine Model Compiler](../mc-java).

 The Error Prone Checks Plugin which is a part of the Model Compiler, performs all necessary 
 configurations and dependency updates to add the `spine-mc-java-checks` to the preprocessor 
 path.
