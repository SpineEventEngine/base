# Spine Protobuf compiler plugin

As Spine relies heavily on Google Protobuf language, it is required  to introduce some kind of 
extensions for the tool. For that, we provide a plugin for the Protobuf compiler 
(a.k.a. `protoc`).

## Features

### Marker interfaces

Spine `protoc` plugin enables the user to mark certain message classes with marker interfaces 
extended from `com.google.protobuf.Message`.

To mark a certain message type, add the `(is)` option for the type:

```proto
import "spine/options.proto";

message Cat {
    option (is) = "org.example.Pet";
    
    // Remainder ommited
}
``` 
After recompiling the definition with the Spine Protobuf plugin, a `.java` file declaring the 
`org.example.Pet` interface will be generated and the `Cat` java class will implement that 
interface.

The file with the interface declaration is generated alongside the message class files.

Similarly to `(is)` option, file-level `(every_is)` option declares that all the message types 
declared in the current file implement the given interface.

```proto
import "spine/options.proto";

option (every_is) = "org.example.Pet";

message Cat {
    // Remainder ommited.
}

message Dog {
    // Remainder ommited.
}

message Hamster {
    // Remainder ommited.
}
```

If both `(is)` and `(every_is)` options are found, `(is)` value is taken.

Also, both `(is)` and `(every_is)` options support shorter syntax with no explicit package 
declaration. In this case, the package of the current file (either `java_package` or Protobuf
`package`) is used.
```proto
package example;

import "spine/options.proto";

option java_package = "org.example.pet";

message Mouse {
    option (is) = "SmallPet";
    
    // Remainder ommited.
}
```

In the example above, the Java FQN of the generated marker interface is `org.example.pet.SmallPet`.
If `java_package` option is absent, the Protobuf package is used instead.

## Usage

To enable the Spine `protoc` plugin, use the Spine Gradle plugin and 
the [Protobuf Gradle plugin](https://github.com/google/protobuf-gradle-plugin):
```groovy
apply plugin: "io.spine.tools.spine-model-compiler"
apply plugin: "com.google.protobuf"
```

Make sure to use the Protobuf Gradle plugin of version `0.8.3` or later.

The Spine Gradle plugin automatically attaches the Spine `protoc` plugin to the Protobuf compilation
process performed by Protobuf Gradle plugin.

For that, the Spine `protoc` plugin artifact is fetched from a maven repository and copied into 
the directory `.spine` found under the project root, so that the artifact relative path is 
`<projectDir>/.spine/spine-protoc-plugin-X.X.X.jar`, where `X.X.X` is the version of the downloaded 
artifact, which is equal to the version of the Spine Gradle plugin used.

To integrate with `protoc`, the shell script [launchers](./plugin_runner.sh) are used.
