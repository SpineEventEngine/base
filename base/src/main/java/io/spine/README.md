# Spine base data types and utilities

This module contains common data types and utilities used by the 
[core-java](https://github.com/SpineEventEngine/core-java) as well as the other Spine Event Engine
projects.

Packages of this module include:

* Common [annotations](annotation) for the Java code.
* Utilities for working with [Java](code/java), [Protobuf](code/proto), 
[Properties](code/properties).
* [Tools](io) for I/O operations.
* JSON [utilities](json).
* Logging [tools](logging) based on [SLF4J](https://www.slf4j.org/).
* [Data types and utilities](money) for working with monetary values.
* [Classes and interfaces](net) for Internet data types.
* [Utilities](option) for working with custom Protobuf Options.
* [Tools](protobuf) for working with proto messages.
* Various [stringifiers](string).
* [Utilities](validate) for validation.

These tools are internal to the Spine project and are not supposed to be utilized in the end user
code. Thus, the API of this module may change at any time.