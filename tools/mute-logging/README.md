# MuteLogging extension

This module contains a `MuteLogging` JUnit extension.

The extension mutes all output produced by a particular JUnit test or test class.

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

## The problem

A seemingly trivial task of muting the test output can become quite complicated when running 
multiple tests in a row.

Most `Logger` implementations are static system-wide tools and in fact have static system-wide 
state.

For example, the 
[standard JDK logger](https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html) 
which we use in tests relies on its `RootLogger` to handle the console output and system 
`out`/`err` streams. The `RootLogger` instance will be the same for multiple class loggers and is 
initialized on the first access to a class-level logger.

Thus, we cannot just redirect `System.out` and `System.err` to some temporary output to remove the 
test logs. By the time we do that, the `RootLogger` will already be initialized with the "correct" 
streams.

This tool thus uses multiple techniques to mute all console output during the test execution. By 
now, in addition to redirection of the standard streams, it mutes all the Spine logging, returning 
the SLF4J `NOPLogger` on every logger request.

This covers most cases where the program output should be muted, allowing for clean project 
builds without overly verbose test stack traces.

Though, if the project uses some non-Spine logger implementation, there may be more complications.
