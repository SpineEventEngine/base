# Spine Model Compiler

This module contains Spine Model Compiler and the associated utilities. 

The Model Compiler assembles the domain model contained in `.proto` files into the Java code. 

It uses the [Spine Protobuf Compiler](../mc-java-protoc) plugin in its core and adds processing of 
various features specific to the Spine application model. 

These features include:

* [Annotating](src/main/java/io/spine/tools/compiler/annotation) Java files generated from the 
  Protobuf definitions.
* [Generating](src/main/java/io/spine/tools/compiler/gen/rejection) Rejections from the `proto` 
  messages.

## Usage

To use the Spine Model Compiler plugin, use it with the 
[Protobuf Gradle plugin](https://github.com/google/protobuf-gradle-plugin):

```groovy
apply plugin: "io.spine.tools.spine-mc-java"
apply plugin: "com.google.protobuf"
```

All operations performed by the Spine Model Compiler plugin are automatically injected in the 
`.proto` compilation process performed by the Protobuf plugin.

The required version of the Protobuf Gradle plugin is `0.8.13` or higher.

## Settings

The generated code is indented with spaces. To set the width, please use the 
`indent` property of the `modelCompiler`:

```groovy
modelCompiler {
    indent = 2
}
```

By default, the value is `4`.

## Spine Error Prone Checks
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

For the Gradle versions older than `4.6` you may need to create and configure the
`annotationProcessor` dependency manually:

```groovy
configurations {
    annotationProcessor
}

dependencies {
    annotationProcessor "io.spine.tools:spine-mc-java-checks:$spineBaseVersion"
}

tasks.withType(JavaCompile) {
    options.compilerArgs += [ '-processorpath', configurations.annotationProcessor.asPath ]
}
```

## Spine Error Prone Checks Plugin
Currently, the Spine-custom Error Prone checks are applied automatically to any project that uses
[Spine Model Compiler](../mc-java).

The Error Prone Checks Plugin which is a part of the Model Compiler, performs all necessary
configurations and dependency updates to add the `spine-mc-java-checks` to the preprocessor
path.

