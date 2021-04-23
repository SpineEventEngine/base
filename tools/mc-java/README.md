# Spine Model Compiler

This module contains Spine Model Compiler and the associated utilities. 

The Model Compiler assembles the domain model contained in `.proto` files into the Java code. 

It uses the [Spine Protobuf Compiler](../protoc-plugin) plugin in its core and adds processing of 
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

