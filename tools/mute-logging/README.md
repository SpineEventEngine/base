# MuteLogging extension

This module contains a `MuteLogging` JUnit extension.

The extension mutes all output produced by the particular JUnit test or test class.

## Usage

To use the extension, add the following dependency to the Gradle project:
```groovy
testImplementation "io.spine.tools:spine-mute-logging:$spineBaseVersion"
```

Then, the extension can be used as follows:
```
@Test
@MuteLogging
void ignoreInvalidClassNames() {
    // ...
}    
```
or
```
@MuteLogging
class ModelVerifierTest {
    // ...
}
```
