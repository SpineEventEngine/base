These tests require configuring `-Xbootclasspath...` option with the path to 
the `com.google.errorprone.javac` JAR.

In Gradle this is done automatically via the separate task (see the `build.gradle` of this module).
To run the tests in IDEA, add the VM options manually in the "Edit configurations" tab. 
Typically, the `javac` JAR can be found in the Gradle `caches` directory in 
the `modules-2/files-2.1/com.google.errorprone/javac/` folder or its subfolders.
After you acquired the path to the existing `javac` JAR, add the following VM option:
```
-Xbootclasspath/p:`javacPath`
```

where the `javacPath` is the path to your `javac` JAR.
For the information about how this test suite works, see the Error Prone [guide](https://github.com/google/error-prone/wiki/Writing-a-check#testing-a-bugchecker)
to testing the custom checks.
