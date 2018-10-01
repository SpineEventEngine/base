# Reflections Plugin

The Gradle port of the
[Maven Reflections plugin](https://mvnrepository.com/artifact/org.reflections/reflections-maven).

Does basically the same as its Maven counterpart (building config for the runtime metadata analysis) 
and is required for the Reflections framework to run.

## Usage

Use the plugin as follows:

```groovy
apply plugin: "io.spine.tools.reflections-plugin"
```
