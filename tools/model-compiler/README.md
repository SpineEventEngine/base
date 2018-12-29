# Spine Model Compiler

This module contains Spine Model Compiler and the associated utilities. 

The Model Compiler assembles the domain model contained in `.proto` files into the Java code. 

It uses the [Spine Protobuf Compiler](../protoc-plugin) plugin in its core and adds processing of 
various features specific to the Spine application model. 

These features include:

* [Annotating](src/main/java/io/spine/tools/compiler/annotation) Java files generated from the 
  Protobuf definitions.
* Event [enrichments](src/main/java/io/spine/tools/compiler/enrichment).
* [Generating](src/main/java/io/spine/tools/compiler/rejection) Rejections from the `proto` 
  messages.
* Various [validation tools](src/main/java/io/spine/tools/compiler/validation) including
  [Validating Builders creation](src/main/java/io/spine/tools/gradle/compiler/ValidatingBuilderGenPlugin.java).

## Usage

To use the Spine Model Compiler plugin, use it with the 
[Protobuf Gradle plugin](https://github.com/google/protobuf-gradle-plugin):

```groovy
apply plugin: "io.spine.tools.spine-model-compiler"
apply plugin: "com.google.protobuf"
```

All operations performed by the Spine Model Compiler plugin are automatically injected in the 
`.proto` compilation process performed by the Protobuf plugin.

The required version of the Protobuf Gradle plugin is `0.8.3` or higher.

## Settings

The validating builders generation can be disabled as follows:

```groovy
modelCompiler {
    generateValidatingBuilders = false
}
```

By default, the value is `true`.

All code in the validating builders is indented with spaces. To set the width, please use the 
`indent` property of the `modelCompiler`:

```groovy
modelCompiler {
    indent = 2
}
```

By default, the value is `4`.

