##Spine Checker
This module contains custom [Error Prone](https://github.com/google/error-prone) checks used in 
Spine. All the checks are automatically included in the `JavaCompile` process and produce the 
corresponding warnings and errors.

##Usage
To use the Spine Checker, add it to the `annotationProcessor` dependency of the project:

```groovy
dependencies {
    annotationProcessor "io.spine.tools:spine-checker:$spineBaseVersion"
}
```

Note that the Checker requires the 
[Error Prone plugin](https://plugins.gradle.org/plugin/net.ltgt.errorprone) applied to the project.

For the Gradle versions older than `4.6` you may need to create and configure the 
`annotationProcessor` dependency manually:

```groovy
configurations {
  annotationProcessor
}

dependencies {
    annotationProcessor "io.spine.tools:spine-checker:$spineBaseVersion"
}

tasks.withType(JavaCompile) {
  options.compilerArgs += [ '-processorpath', configurations.annotationProcessor.asPath ]
}
```

##Spine Checker Plugin
Currently, the Spine Checker is applied automatically to any project that uses 
[Spine Model Compiler](../model-compiler).

The Spine Checker Plugin which is a part of the Model Compiler, performs all necessary 
configurations automatically.