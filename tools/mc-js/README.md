# Proto JS plugin

The Gradle plugin which assists the Protobuf JS compiler in code generation.

Currently, the plugin only purpose is adding `fromJson(json)` and `fromObject(object)` methods to 
the generated messages. These methods allow to parse the message from the JSON `string` and 
JavaScript `Object` respectively.

## Usage

To use the plugin, add it to the `classpath` configuration of the `buildscript` as follows:

```groovy
buildscript {
    dependencies {
        classpath "io.spine.tools:spine-proto-js-plugin:$spineBaseVersion"
    }
}
```

The plugin may then be applied where necessary:

```groovy
apply plugin: "io.spine.tools.proto-js-plugin"
```

The `protoJs` extension provides access to the main plugin action. It may then be used to configure 
when the action will be executed relative to other tasks. Example:

```groovy
protoJs {
    generateParsersTask().dependsOn compileProtoToJs
    compileJs.dependsOn generateParsersTask()
}
```

By default, the plugin action will just be a dependency of the `build` task.

## Required configurations

These settings are:

1. The `CommonJS` import style for the generated JavaScript files 
   (`js {option "import_style=commonjs"}` in `protobuf` extension).
1. The task `compileProtoToJs` available in the project and being
 the dependency of the `build` task.

